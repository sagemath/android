package org.sagemath.singlecellserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Time;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;


/**
 * Interface with the Sage single cell server
 * 
 * @author vbraun
 *
 */
public class SageSingleCell {
	private final static String TAG = "SageSingleCell";

	private long timeout  = 30*1000;

	// private String server = "http://localhost:8001";
	private String server = "http://sagecell.sagemath.org/kernel";
	private String server_path_eval = "/eval";
	private String server_path_output_poll = "/output_poll";
	private String server_path_files = "/files";


	protected boolean downloadDataFiles = true;

	/**
	 * Whether to immediately download data files or only save their URI
	 *  
	 * @param value Download immediately if true
	 */
	public void setDownloadDataFiles(boolean value) {
		downloadDataFiles = value;
	}

	/**
	 * Set the server
	 * 
	 * @param server The server, for example "http://sagemath.org:5467"
	 * @param eval The path on the server for the eval post, for example "/eval"
	 * @param poll The path on the server for the output polling, for example "/output_poll"
	 */
	public void setServer(String server, String eval, String poll, String files) {
		this.server = server;
		this.server_path_eval = eval;
		this.server_path_output_poll = poll;
		this.server_path_files = files;
	}

	public interface OnSageListener {

		/** 
		 * Output in a new block or an existing block where all current entries are supposed to be erased 
		 * @param output
		 */
		public void onSageOutputListener(CommandOutput output);

		/**
		 * Output to add to an existing output block
		 * @param output
		 */
		public void onSageAdditionalOutputListener(CommandOutput output);

		/**
		 * Callback for an interact_prepare message
		 * @param interact The interact
		 */
		public void onSageInteractListener(Interact interact);


		/**
		 * The Sage session has been closed
		 * @param reason A SessionEnd message or a HttpError 
		 */
		public void onSageFinishedListener(CommandReply reason);
	}

	private OnSageListener listener;

	/**
	 * Set the result callback, see {@link #query(String)}
	 * 
	 * @param listener
	 */
	public void setOnSageListener(OnSageListener listener) {
		this.listener = listener;
	}

	public SageSingleCell() {
		logging();
	}

	public void logging() { 
		// You also have to
		// adb shell setprop log.tag.httpclient.wire.header VERBOSE
		// adb shell setprop log.tag.httpclient.wire.content VERBOSE
		java.util.logging.Logger apacheWireLog = java.util.logging.Logger.getLogger("org.apache.http.wire");
		apacheWireLog.setLevel(java.util.logging.Level.FINEST);
	}


	protected static String streamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
		return sb.toString();
	}

	public static class SageInterruptedException extends Exception {
		private static final long serialVersionUID = -5638564842011063160L;
	}

	LinkedList<ServerTask> threads = new LinkedList<ServerTask>();

	private void addThread(ServerTask thread) {
		synchronized (threads) {
			threads.add(thread);
		}
	}

	private void removeThread(ServerTask thread) {
		synchronized (threads) {
			threads.remove(thread);
		}
	}

	public enum LogLevel { NONE, BRIEF, VERBOSE };

	private LogLevel logLevel = LogLevel.NONE;

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public class ServerTask extends Thread {
		private final static String TAG = "ServerTask";

		private final UUID session;
		private final String sageInput;
		private boolean sendOnly = false;
		private final boolean sageMode = true; 
		private boolean interrupt = false;
		private DefaultHttpClient httpClient;
		private Interact interact;
		private CommandRequest request, currentRequest;
		private LinkedList<String> outputBlocks = new LinkedList<String>();
		private long initialTime = System.currentTimeMillis();
		private String kernel_url;
		private String shell_url;
		private String iopub_url;
		private WebSocketClient shellclient;
		private WebSocketClient iopubclient;

		protected LinkedList<CommandReply> result = new LinkedList<CommandReply>();

		protected void log(Command command) {
			if (logLevel.equals(LogLevel.NONE)) return;
			String s;
			if (command instanceof CommandReply)
				s = ">> ";
			else if (command instanceof CommandRequest) 
				s = "<< ";
			else
				s = "== ";
			long t = System.currentTimeMillis() - initialTime;
			s += "(" + String.valueOf(t) + "ms) ";
			s += command.toShortString();
			if (logLevel.equals(LogLevel.VERBOSE)) {
				s += " ";
				s += command.toLongString();
				s += "\n";
			}
			System.out.println(s);
			System.out.flush();
		}

		/**
		 * Whether to only send or also receive the replies
		 * @param sendOnly
		 */
		protected void setSendOnly(boolean sendOnly) {
			this.sendOnly = sendOnly;
		}

		protected void addReply(CommandReply reply) {
			log(reply);
			result.add(reply);
			if (reply.isInteract()) {
				Log.i(TAG, "addReply(reply): Reply is an interact.");
				interact = (Interact) reply;
				listener.onSageInteractListener(interact);
			}
			else if (reply.containsOutput() && reply.isReplyTo(currentRequest)) {
				Log.i(TAG, "addReply(reply): Reply is response to currentRequest.");
				CommandOutput output = (CommandOutput) reply;
				if (outputBlocks.contains(output.outputBlock())) {
					Log.i(TAG,"Output contains an output block");
					listener.onSageAdditionalOutputListener(output);
				}
				else {
					Log.i(TAG,"Added an output block");
					outputBlocks.add(output.outputBlock());
					listener.onSageOutputListener(output);
				}
			}
		}

		/**
		 * The timeout for the http request
		 * @return timeout in milliseconds
		 */
		public long timeout() {
			return timeout;
		}

		private void init() {
			Log.i(TAG, "SageSingleCell.init() called");
			HttpParams params = new BasicHttpParams();
			params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			httpClient = new DefaultHttpClient(params);
			addThread(this);
			currentRequest = request = new ExecuteRequest(sageInput, sageMode, session);
		}

		public ServerTask() {
			this.sageInput = null;
			this.session = null;
			init();
		}

		public ServerTask(String sageInput) {
			this.sageInput = sageInput;
			this.session = null;
			init();
		}

		public ServerTask(String sageInput, UUID session) {
			this.sageInput = sageInput;
			this.session = session;
			init();
		}

		public void interrupt() {
			interrupt = true;
		}

		public boolean isInterrupted() {
			return interrupt;
		}

		protected HttpResponse postEval(JSONObject request)
				throws ClientProtocolException, IOException, SageInterruptedException, JSONException, URISyntaxException {
			if (interrupt) throw new SageInterruptedException(); 
			Log.i(TAG, "SageSingleCell: postEval() called\n");
			//HttpGet httpget = new HttpGet(server);
			//httpget.addHeader("Accept", "application/json");
			HttpPost httpPost = new HttpPost(server);
			URI absolute = new URI("http://sagecell.sagemath.org/");
			URI relative = new URI("kernel");
			httpPost.setURI(absolute.resolve(relative));
			httpPost.addHeader("Accept-Econding", "identity");


			//httpPost.setHeader("Accept", "application/json");
			//MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			//httpPost.setEntity(multipartEntity);
			HttpResponse httpResponse = httpClient.execute(httpPost);

			InputStream outputStream = httpResponse.getEntity().getContent();
			String output = SageSingleCell.streamToString(outputStream);
			outputStream.close();

			Log.i(TAG, "output = " + output);
			JSONObject outputJSON = new JSONObject(output);

			if (outputJSON.has("kernel_id") & outputJSON.has("ws_url")) {
				String kernel_id = outputJSON.getString("kernel_id");
				String ws_url = outputJSON.getString("ws_url"); 
				kernel_url = ws_url + "kernel/" + kernel_id.toString() + "/";
				shell_url = kernel_url + "shell";
				iopub_url = kernel_url + "iopub";
				Log.i(TAG, "Kernel URL: " + kernel_url);
				Log.i(TAG, "Shell URL: " + shell_url);
				Log.i(TAG, "iopub URL: " + iopub_url);
			}

			initializeSockets();
			
			sendInitialMessage(request.toString());

			return httpResponse;
		}

		protected void initializeSockets() {
			
			shellclient = new WebSocketClient(URI.create(shell_url), new WebSocketClient.Listener() {
				@Override
				public void onConnect() {
					Log.d(TAG, "shell socket connected!");
				}

				@Override
				public void onMessage(String message) {
					Log.d(TAG, String.format("Got string message from shell!\n%s", message));
				}

				@Override
				public void onMessage(byte[] data) {
					Log.d(TAG, String.format("Got binary message! %s"));
				}

				@Override
				public void onDisconnect(int code, String reason) {
					Log.d(TAG, String.format("Disconnected! Code: %d Reason: %s", code, reason));
				}

				@Override
				public void onError(Exception error) {
					Log.e(TAG, "Error!", error);
				}

			}, null);
			
			iopubclient = new WebSocketClient(URI.create(iopub_url), new WebSocketClient.Listener() {
				@Override
				public void onConnect() {
					Log.d(TAG, "iopub socket connected!");
				}

				@Override
				public void onMessage(String message) {
					Log.d(TAG, String.format("Got string message from iopub!\n%s", message));
					try {
						JSONObject JSONreply = new JSONObject(message);
						CommandReply reply = new CommandReply(JSONreply);
						addReply(reply);
						Log.i(TAG, "Tried to add reply");
					} catch (JSONException e) {
						Log.e(TAG, "Had trouble parsing the JSON reply...");
					}
					
				}

				@Override
				public void onMessage(byte[] data) {
					Log.d(TAG, String.format("Got binary message! %s"));
				}

				@Override
				public void onDisconnect(int code, String reason) {
					Log.d(TAG, String.format("Disconnected! Code: %d Reason: %s", code, reason));
				}

				@Override
				public void onError(Exception error) {
					Log.e(TAG, "Error!", error);
				}

			}, null);

			shellclient.connect();
			iopubclient.connect();
			
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				Log.i(TAG, "Couldn't sleep :(");
			}
			
		}

		protected void sendInitialMessage(String message){
			//String Stringmessage = "{\"content\": {\"user_variables\": [], \"allow_stdin\": false, \"code\": \""+sageInput+"\", \"silent\": false, \"user_expressions\": {\"_sagecell_files\": \"sys._sage_.new_files()\"}}, \"header\": {\"username\": \"\", \"msg_id\": \""+request.msg_id+"\", \"session\": \""+request.session+"\", \"msg_type\": \"execute_request\"}, \"parent_header\": {}, \"metadata\": {}}";
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				Log.i(TAG, "Couldn't sleep...");
			}
			try {
				shellclient.wait(1000);
			} catch (Exception e) {
				Log.i(TAG, "Couldn't sleep...");
			}
			
			
			shellclient.send(message);
			Log.i(TAG, "Tried to send message:\n" + message);
		}

/*
		protected HttpResponse pollOutput(CommandRequest request, int sequence) 
				throws ClientProtocolException, IOException, SageInterruptedException {
			if (interrupt) throw new SageInterruptedException(); 
			Time time = new Time();
			StringBuilder query = new StringBuilder();
			time.setToNow();
			// query.append("?callback=jQuery");
			query.append("?computation_id=" + request.session.toString());
			query.append("&sequence=" + sequence);
			query.append("&rand=" + time.toMillis(true));
			HttpGet httpGet = new HttpGet(server + server_path_output_poll + query);
			return httpClient.execute(httpGet);
		}
*/

		//	    protected void callSageOutputListener(CommandOutput output) {
		//			listener.onSageOutputListener(output);
		//	    }
		//	    
		//	    protected void callSageReplaceOutputListener(CommandOutput output) {
		//			listener.onSageReplaceOutputListener(output);
		//	    }
		//	    
		//	    protected void callSageInteractListener(Interact interact) {
		//			listener.onSageInteractListener(interact);
		//	    }

		protected URI downloadFileURI(CommandReply reply, String filename) throws URISyntaxException {
			StringBuilder query = new StringBuilder();
			query.append("/"+reply.session.toString());
			query.append("/"+filename);
			return new URI(server + server_path_files + query);
		}

		protected HttpResponse downloadFile(URI uri)
				throws ClientProtocolException, IOException, SageInterruptedException {
			if (interrupt) throw new SageInterruptedException(); 
			HttpGet httpGet = new HttpGet(uri);
			return httpClient.execute(httpGet);
		}

		protected boolean downloadDataFiles() {
			return SageSingleCell.this.downloadDataFiles;
		}

		@Override
		public void run() {
			super.run();
			Log.i(TAG, "SageSingleCell run() called");
			log(request);
			if (sendOnly) {
				request.sendRequest(this);
				removeThread(this);
				return;
			}
			request.sendRequest(this);
			removeThread(this);
			//listener.onSageFinishedListener(result.getLast());
		}
	}


	/**
	 * Start an asynchronous query on the Sage server
	 * The result will be handled by the callback set by {@link #setOnSageListener(OnSageListener)}
	 * @param sageInput
	 */
	public void query(String sageInput) {
		Log.i(TAG, "sageInput is " + sageInput);
		ServerTask task = new ServerTask(sageInput);
		task.start();
	}

	/**
	 * Update an interactive element 
	 * @param interact  The interact_prepare message we got from the server as we set up the interact
	 * @param name      The name of the variable in the interact function declaration
	 * @param value     The new value
	 */
	public void interact(Interact interact, String name, Object value) {
		String sageInput = 
				"_update_interact('" + interact.getID() + 
				"',control_vals=dict(" + name + 
				"=" + value.toString() + ",))";
		ServerTask task = new ServerTask(sageInput, interact.session);
		synchronized (threads) {
			for (ServerTask thread: threads)
				if (thread.interact == interact) {
					thread.currentRequest = task.request;
					thread.outputBlocks.clear();
				}
		}
		task.setSendOnly(true);
		task.start();
	}

	/**
	 *  Interrupt all pending Sage server transactions
	 */
	public void interrupt() {
		synchronized (threads) {
			for (ServerTask thread: threads)
				thread.interrupt();
		}
	}


	/**
	 * Whether a computation is currently running
	 * 
	 * @return
	 */
	public boolean isRunning() {
		synchronized (threads) {
			for (ServerTask thread: threads)
				if (!thread.isInterrupted())
					return true;
		}
		return false;
	}
}


/*

=== Send request === 

POST /eval HTTP/1.1
Host: localhost:8001
Connection: keep-alive
Content-Length: 506
Cache-Control: max-age=0
Origin: http://localhost:8001
User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.121 Safari/535.2
Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryh9NcFTBy2FksKYpN
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,* / *;q=0.8
Referer: http://localhost:8001/
Accept-Encoding: gzip,deflate,sdch
Accept-Language: en-US,en;q=0.8,de;q=0.6,ja;q=0.4,fr-FR;q=0.2,he;q=0.2,ga;q=0.2,ko;q=0.2
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
Cookie: DWd8c32a438995fbf98bd158172221d77e=dmJyYXVu%7C1%7CWHhzTnc5M0NXZDZyekQzcjlVL1lNZz09; DOKU_PREFS=sizeCtl%23679px

------WebKitFormBoundaryh9NcFTBy2FksKYpN
Content-Disposition: form-data; name="commands"

"1+1"
------WebKitFormBoundaryh9NcFTBy2FksKYpN
Content-Disposition: form-data; name="session_id"

87013257-7c34-4c83-b7ed-2ec7e7480935
------WebKitFormBoundaryh9NcFTBy2FksKYpN
Content-Disposition: form-data; name="msg_id"

489696dc-ed8d-4cb6-a140-4282a43eda95
------WebKitFormBoundaryh9NcFTBy2FksKYpN
Content-Disposition: form-data; name="sage_mode"

True
------WebKitFormBoundaryh9NcFTBy2FksKYpN--
HTTP/1.1 200 OK
Server: nginx/1.0.4
Date: Mon, 12 Dec 2011 19:12:14 GMT
Content-Type: text/html; charset=utf-8
Connection: keep-alive
Content-Length: 0


=== Poll for reply (unsuccessfully, try again) ===

GET /output_poll?callback=jQuery15015045171417295933_1323715011672&computation_id=25508880-6a6a-4353-9439-689468ec679e&sequence=0&_=1323718075839 HTTP/1.1
Host: localhost:8001
Connection: keep-alive
x-requested-with: XMLHttpRequest
User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.121 Safari/535.2
accept: text/javascript, application/javascript, * / *; q=0.01
Referer: http://localhost:8001/
Accept-Encoding: gzip,deflate,sdch
Accept-Language: en-US,en;q=0.8,de;q=0.6,ja;q=0.4,fr-FR;q=0.2,he;q=0.2,ga;q=0.2,ko;q=0.2
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
Cookie: DWd8c32a438995fbf98bd158172221d77e=dmJyYXVu%7C1%7CWHhzTnc5M0NXZDZyekQzcjlVL1lNZz09; DOKU_PREFS=sizeCtl%23679px

HTTP/1.1 200 OK
Server: nginx/1.0.4
Date: Mon, 12 Dec 2011 19:27:55 GMT
Content-Type: text/javascript; charset=utf-8
Connection: keep-alive
Content-Length: 44

jQuery15015045171417295933_1323715011672({})



=== Poll for reply (success) ===

GET /output_poll?callback=jQuery15015045171417295933_1323715011662&computation_id=87013257-7c34-4c83-b7ed-2ec7e7480935&sequence=0&_=1323717134406 HTTP/1.1
Host: localhost:8001
Connection: keep-alive
x-requested-with: XMLHttpRequest
User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.121 Safari/535.2
accept: text/javascript, application/javascript, * / *; q=0.01
Referer: http://localhost:8001/
Accept-Encoding: gzip,deflate,sdch
Accept-Language: en-US,en;q=0.8,de;q=0.6,ja;q=0.4,fr-FR;q=0.2,he;q=0.2,ga;q=0.2,ko;q=0.2
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
Cookie: DWd8c32a438995fbf98bd158172221d77e=dmJyYXVu%7C1%7CWHhzTnc5M0NXZDZyekQzcjlVL1lNZz09; DOKU_PREFS=sizeCtl%23679px

=== Reply with result from server ===

HTTP/1.1 200 OK
Server: nginx/1.0.4
Date: Mon, 12 Dec 2011 19:12:14 GMT
Content-Type: text/javascript; charset=utf-8
Connection: keep-alive
Content-Length: 830

jQuery15015045171417295933_1323715011662(
{"content": [{
  "parent_header": 
  {
    "username": "", 
    "msg_id": "489696dc-ed8d-4cb6-a140-4282a43eda95", 
    "session": "87013257-7c34-4c83-b7ed-2ec7e7480935"}, 
  "msg_type": "pyout", 
  "sequence": 0, 
  "output_block": null, 
  "content": {
    "data": {"text/plain": "2"}}, 
  "header": {"msg_id": "1620524024608841996"}}, 
  {
    "parent_header": 
    {
      "username": "", 
      "msg_id": "489696dc-ed8d-4cb6-a140-4282a43eda95", 
      "session": "87013257-7c34-4c83-b7ed-2ec7e7480935"}, 
    "msg_type": "execute_reply", 
    "sequence": 1, 
    "output_block": null, 
    "content": {"status": "ok"}, 
    "header": {"msg_id": "1501182239947896697"}}, 
    {
      "parent_header": {"session": "87013257-7c34-4c83-b7ed-2ec7e7480935"}, 
      "msg_type": "extension", 
      "sequence": 2, 
      "content": {"msg_type": "session_end"}, 
      "header": {"msg_id": "1e6db71f-61a0-47f9-9607-f2054243bb67"}
}]})



=== Reply with Syntax Erorr ===

GET /output_poll?callback=jQuery15015045171417295933_1323715011664&computation_id=9b3ed6bb-01e8-4a6e-9076-14c71324daf6&sequence=0&_=1323717902557 HTTP/1.1
Host: localhost:8001
Connection: keep-alive
x-requested-with: XMLHttpRequest
User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.121 Safari/535.2
accept: text/javascript, application/javascript, * / *; q=0.01
Referer: http://localhost:8001/
Accept-Encoding: gzip,deflate,sdch
Accept-Language: en-US,en;q=0.8,de;q=0.6,ja;q=0.4,fr-FR;q=0.2,he;q=0.2,ga;q=0.2,ko;q=0.2
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
Cookie: DWd8c32a438995fbf98bd158172221d77e=dmJyYXVu%7C1%7CWHhzTnc5M0NXZDZyekQzcjlVL1lNZz09; DOKU_PREFS=sizeCtl%23679px

HTTP/1.1 200 OK
Server: nginx/1.0.4
Date: Mon, 12 Dec 2011 19:25:02 GMT
Content-Type: text/javascript; charset=utf-8
Connection: keep-alive
Content-Length: 673

jQuery15015045171417295933_1323715011664({"content": [{"parent_header": {"username": "", "msg_id": "df56f48a-d47f-4267-b228-77f051d7d834", "session": "9b3ed6bb-01e8-4a6e-9076-14c71324daf6"}, "msg_type": "execute_reply", "sequence": 0, "output_block": null, "content": {"status": "error", "ename": "SyntaxError", "evalue": "invalid syntax", "traceback": ["\u001b[1;31m---------------------------------------------------------------------------\u001b[0m\n\u001b[1;31mSyntaxError\u001b[0m                               Traceback (most recent call last)", "\u001b[1;31mSyntaxError\u001b[0m: invalid syntax (<string>, line 39)"]}, "header": {"msg_id": "2853508955959610959"}}]})GET /output_poll?callback=jQuery15015045171417295933_1323715011665&computation_id=9b3ed6bb-01e8-4a6e-9076-14c71324daf6&sequence=1&_=1323717904769 HTTP/1.1
Host: localhost:8001
Connection: keep-alive
x-requested-with: XMLHttpRequest
User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.121 Safari/535.2
accept: text/javascript, application/javascript, * / *; q=0.01
Referer: http://localhost:8001/
Accept-Encoding: gzip,deflate,sdch
Accept-Language: en-US,en;q=0.8,de;q=0.6,ja;q=0.4,fr-FR;q=0.2,he;q=0.2,ga;q=0.2,ko;q=0.2
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
Cookie: DWd8c32a438995fbf98bd158172221d77e=dmJyYXVu%7C1%7CWHhzTnc5M0NXZDZyekQzcjlVL1lNZz09; DOKU_PREFS=sizeCtl%23679px

HTTP/1.1 200 OK
Server: nginx/1.0.4
Date: Mon, 12 Dec 2011 19:25:04 GMT
Content-Type: text/javascript; charset=utf-8
Connection: keep-alive
Content-Length: 269

jQuery15015045171417295933_1323715011665({"content": [{"parent_header": {"session": "9b3ed6bb-01e8-4a6e-9076-14c71324daf6"}, "msg_type": "extension", "sequence": 1, "content": {"msg_type": "session_end"}, "header": {"msg_id": "e01180d4-934c-4f12-858c-72d52e0330cd"}}]})

 */