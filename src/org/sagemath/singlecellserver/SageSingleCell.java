package org.sagemath.singlecellserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

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
	private String server = "https://sagecell.sagemath.org/kernel";
	private String server_path_eval = "/eval";
	private String server_path_output_poll = "/output_poll";
	private String server_path_files = "/files";
	private URI shareURI;
	ServerTask task;

	protected boolean downloadDataFiles = true;

	/**
	 * Whether to immediately download data files or only save their URI
	 *  
	 * @param value Download immediately if true
	 */
	public void setDownloadDataFiles(boolean value) {
		Log.i(TAG, "Tried to setDownloadDataFiles set to " + String.valueOf(value));
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


	public static String streamToString(InputStream is) {
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
	
	public enum LogLevel { NONE, BRIEF, VERBOSE };

	private LogLevel logLevel = LogLevel.NONE;

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public class ServerTask {
		private final static String TAG = "ServerTask";

		private UUID session;
		private String sageInput;
		private boolean sendOnly = false;
		private final boolean sageMode = true; 
		private boolean interrupt = false;
		private DefaultHttpClient httpClient;
		private Interact interact;
		private CommandRequest request, currentRequest;
		private LinkedList<String> outputBlocks = new LinkedList<String>();
		private long initialTime = System.currentTimeMillis();
		private WebSocketClient shellclient;
		private WebSocketClient iopubclient;
		private String kernel_url;
		private String shell_url;
		private String iopub_url;

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

			//Log.i(TAG, "addReply successfully received a CommandReply");
			log(reply);
			if (reply instanceof DataFile) {
				try {
				((DataFile) reply).downloadFile(this);
				} catch (Exception e) {
					Log.e(TAG, "Error download file:");
					e.printStackTrace();
				}
			}
			//Log.i(TAG, "reply.isReplyTo(currentRequest): " + String.valueOf(reply.isReplyTo(currentRequest)));
			result.add(reply);
			if (reply.isInteract()) {
				//Log.i(TAG, "addReply(reply): Reply is an interact.");
				interact = (Interact) reply;
				listener.onSageInteractListener(interact);
			} else if (reply.containsOutput() && reply.isReplyTo(currentRequest)) {
				//Log.i(TAG, "addReply(reply): Reply is response to currentRequest.");
				CommandOutput output = (CommandOutput) reply;
				if (outputBlocks.contains(output.outputBlock())) {
					//Log.i(TAG,"Output contains an output block");
					listener.onSageAdditionalOutputListener(output);
				}
				else {
					//Log.i(TAG,"Added an output block");
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
			
			currentRequest = request = new ExecuteRequest(sageInput, sageMode, session);
			
			try {
				shareURI = getShareURI(sageInput);
			} catch (Exception e) {
				Log.e(TAG, "Error getting Share URI" + e.getLocalizedMessage());
			}
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
		
		public ServerTask(String sageInput, UUID session, String kernel_url) {
			// Same as the other ServerTask method for updating interacts,
			// except without running a new postEval -- just initializeSockets
			// and send the updated message.
			this.sageInput = sageInput;
			this.session = session;		
			this.shell_url = kernel_url + "shell";
			this.iopub_url = kernel_url + "iopub";
		}

		public void interrupt() {
			interrupt = true;
		}

		public boolean isInterrupted() {
			return interrupt;
		}
		
		public URI getShareURI(String sageInput) throws 
				JSONException, URISyntaxException, ClientProtocolException, IOException {
			URI shareURI = new URI("https://sagecell.sagemath.org");
			URI absolute = new URI("https://sagecell.sagemath.org");
			URI permalinkRelative = new URI("/permalink");
			URI permalinkURI = absolute.resolve(permalinkRelative);
			
			HttpPost permalinkPost = new HttpPost();
			permalinkPost.setURI(permalinkURI);
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("code", sageInput));
		    permalinkPost.setEntity(new UrlEncodedFormEntity(postParameters));
		    
		    HttpParams params = new BasicHttpParams();
			params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			
		    HttpClient shareHttpClient = new DefaultHttpClient(params);
		    HttpResponse httpResponse = shareHttpClient.execute(permalinkPost);
			InputStream outputStream = httpResponse.getEntity().getContent();
			String output = SageSingleCell.streamToString(outputStream);
			outputStream.close();
			
			Log.i(TAG, "output = " + output);
			JSONObject outputJSON = new JSONObject(output);

			if (outputJSON.has("query")) {
				String query_id = outputJSON.getString("query");
				URI shareURIRelative = new URI("/?q=" + query_id);
				shareURI = shareURI.resolve(shareURIRelative);
			}
			Log.i(TAG, "Share URI: " + shareURI);
			
			return shareURI;
		}

		protected HttpResponse postEval(JSONObject request)
				throws ClientProtocolException, IOException, SageInterruptedException, JSONException, URISyntaxException {
			if (interrupt) throw new SageInterruptedException(); 
			Log.i(TAG, "SageSingleCell: postEval() called\n");
			HttpPost httpPost = new HttpPost();
			URI absolute = new URI("https://sagecell.sagemath.org");
			//URI(String scheme, String userInfo, String host, int port, String path, String query, String fragment)
			URI kernelRelative = new URI("/kernel");
			URI sageURI = absolute.resolve(kernelRelative);
			
			/*
			// To construct a URI with a port (for testing on http://sagecell.sagemath.org:10080):
			//URI(String scheme, String userInfo, String host, int port, String path, String query, String fragment)
			int port = 10080;
			URI testURI = new URI(sageURI.getScheme(), sageURI.getUserInfo(), sageURI.getHost(), port, 
					sageURI.getPath(), sageURI.getQuery(), sageURI.getFragment());
			Log.i(TAG, "Test URI: " + testURI.toString());
			//httpPost.setURI(testURI);
			*/
			
			Log.i(TAG, "Sage URI: " + sageURI.toString());
			httpPost.setURI(sageURI);
			
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
		    postParameters.add(new BasicNameValuePair("Accept-Econding", "identity"));
		    postParameters.add(new BasicNameValuePair("accepted_tos", "true"));
		    httpPost.setEntity(new UrlEncodedFormEntity(postParameters));


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
				//Log.i(TAG, "Kernel URL: " + kernel_url);
				//Log.i(TAG, "Shell URL: " + shell_url);
				//Log.i(TAG, "iopub URL: " + iopub_url);
			}

			initializeSockets();
			
			//Log.i(TAG, "shellclient sending request.toString(): " + request.toString());
			shellclient.send(request.toString());

			return httpResponse;
		}

		protected void initializeSockets() {
			//Log.i(TAG, "Initializing socket with shell_url: " + shell_url);
			shellclient = new WebSocketClient(URI.create(shell_url), new WebSocketClient.Listener() {
				@Override
				public void onConnect() {
					Log.d(TAG, "shell socket connected!");
				}
				@Override
				public void onMessage(String message) {
					//Log.d(TAG, String.format("Got string message from shell!"));
					//Log.d(TAG, String.format("Got string message from shell!\n%s", message));
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
					//Log.d(TAG, String.format("Got string message from iopub!\n"));
					try {
						//Log.i(TAG, "Trying to add reply");
						JSONObject JSONreply = new JSONObject(message);
						CommandReply reply = CommandReply.parse(JSONreply);
						addReply(reply);
					} catch (JSONException e) {
						Log.e(TAG, "Had trouble parsing the JSON reply...");
						e.printStackTrace();
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
				Log.i(TAG, "Couldn't sleep in initializeSockets");
			}
			
		}

		protected URI downloadFileURI(CommandReply reply, String filename) throws URISyntaxException {
			Log.i(TAG, "SageSingleCell.downloadFileURI called for " + filename);
			String fileurl = kernel_url.replace("ws", "http") + "files/" + filename;
			Log.i(TAG, "Final URI is: " + fileurl);
			return new URI(fileurl);
		}

		protected HttpResponse downloadFile(URI uri)
				throws ClientProtocolException, IOException, SageInterruptedException {
			Log.i(TAG, "downloadFile called with URI " + uri.toString());
			if (interrupt) throw new SageInterruptedException(); 
			HttpGet httpGet = new HttpGet(uri);
			return httpClient.execute(httpGet);
		}

		protected boolean downloadDataFiles() {
			return SageSingleCell.this.downloadDataFiles;
		}

		public void start() {
			//Previously a thread's runnable.
			//super.run();
			Log.i(TAG, "SageSingleCell run() called");
			log(request);
			request.sendRequest(this);
			return;
		}
	}


	/**
	 * Start an asynchronous query on the Sage server
	 * The result will be handled by the callback set by {@link #setOnSageListener(OnSageListener)}
	 * @param sageInput
	 */
	public void query(String sageInput) {
		Log.i(TAG, "sageInput is " + sageInput);
		task = new ServerTask(sageInput);
		task.start();
	}
	
	public URI getShareURI() {
		return shareURI;
	}

	/**
	 * Update an interactive element 
	 * @param interact  The interact_prepare message we got from the server as we set up the interact
	 * @param name      The name of the variable in the interact function declaration
	 * @param value     The new value
	 */
	public void interact(Interact interact, String name, Object value) {
		Log.i(TAG, "UPDATING INTERACT VARIABLE: " + name);
		Log.i(TAG, "UPDATED INTERACT VALUE: " + value.toString());
		
		String sageInput = 
				"sys._sage_.update_interact(\"" + interact.getID() + 
				"\",\"" + name + 
				"\"," + value.toString() + ")";

		task.currentRequest = task.request = new ExecuteRequest(sageInput, true, interact.session);

		String message = "";
		
		try {
			message = task.request.toJSON().toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		Log.i(TAG, "SageSingleCell.interact() trying to send message...");
		task.shellclient.send(message);
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