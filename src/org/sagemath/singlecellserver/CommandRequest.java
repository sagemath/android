package org.sagemath.singlecellserver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.singlecellserver.SageSingleCell.SageInterruptedException;

public class CommandRequest extends Command {
	private static final String TAG = "CommandRequest";
	
	long time = System.currentTimeMillis();

	private int SLEEP_BEFORE_TRY = 20;
	
	private boolean error = false;
	
	public CommandRequest() {
		super();
	}
	
	public CommandRequest(UUID session) {
		super(session);
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject header = new JSONObject();
		header.put("session", session.toString());
		header.put("msg_id", msg_id.toString());
		JSONObject result = new JSONObject();
		result.put("header", header);
		return result;
	}
	
	
	protected void sendRequest(SageSingleCell.ServerTask server) {
		CommandReply reply;
		try {
			HttpResponse httpResponse = server.postEval(toJSON());
			//processInitialReply(httpResponse);
			return;
		} catch (JSONException e) {
			reply = new HttpError(this, e.getLocalizedMessage());
		} catch (ClientProtocolException e) {
			reply = new HttpError(this, e.getLocalizedMessage());
		} catch (IOException e) {
			reply = new HttpError(this, e.getLocalizedMessage());
		} catch (SageInterruptedException e) {
			reply = new HttpError(this, "Interrupted on user request");
		} catch (URISyntaxException e) {
			reply = new HttpError(this, e.getLocalizedMessage());
		}
		error = true;
		server.addReply(reply);
	}
	
	
	/*public StatusLine processInitialReply(HttpResponse response) 
			throws IllegalStateException, IOException, JSONException {
		System.out.println("CommandRequest: processInitialReply called\n");
        //InputStream outputStream = response.getEntity().getContent();
        //String output = SageSingleCell.streamToString(outputStream);
        //outputStream.close();

		
        Log.i(TAG, "\n\n\n-----INITIAL RESPONSE-------\n" + response.toString());
        Log.i(TAG, "----END INITIAL RESPONSE----\n");
        
        InputStream outputStream = response.getEntity().getContent();
        String output = SageSingleCell.streamToString(outputStream);
        outputStream.close();

        /*
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
        
        //JSONObject outputJSON = new JSONObject(output);
        //if (outputJSON.has("session_id"))
        	//session = UUID.fromString(outputJSON.getString("session_id"));
        
        
        

        return response.getStatusLine();
	}*/
	
	/*
	public void receiveReply(SageSingleCell.ServerTask server) {
		sendRequest(server);
		if (error) return;
		long timeEnd = System.currentTimeMillis() + server.timeout();
		int count = 0;
		while (System.currentTimeMillis() < timeEnd) {
			count++;
			LinkedList<CommandReply> result = pollResult(server); 
			// System.out.println("Poll got "+ result.size());
			ListIterator<CommandReply> iter = result.listIterator();
			while (iter.hasNext()) {
				CommandReply reply = iter.next();
				timeEnd += reply.extendTimeOut();
				server.addReply(reply);
	    	}	
			if (error) return;
			if (!result.isEmpty() && (result.getLast().terminateServerConnection()))
				return;
	    	try {
	    		Thread.sleep(count*SLEEP_BEFORE_TRY);
			} catch (InterruptedException e) {}
	    }
		error = true;
		server.addReply(new HttpError(this, "Timeout"));
	}

	
	private LinkedList<CommandReply> pollResult(SageSingleCell.ServerTask server) {
		LinkedList<CommandReply> result = new LinkedList<CommandReply>();
		try {
			int sequence = server.result.size();
			result.addAll(pollResult(server, sequence)); 
			return result;
		} catch (JSONException e) {
			CommandReply reply = new HttpError(this, e.getLocalizedMessage());
			result.add(reply);
		} catch (ClientProtocolException e) {
			CommandReply reply = new HttpError(this, e.getLocalizedMessage());
			result.add(reply);
		} catch (IOException e) {
			CommandReply reply = new HttpError(this, e.getLocalizedMessage());
			result.add(reply);
		} catch (URISyntaxException e) {
			CommandReply reply = new HttpError(this, e.getLocalizedMessage());
			result.add(reply);
		} catch (SageInterruptedException e) {
			CommandReply reply = new HttpError(this, "Interrupted on user request");
			result.add(reply);
		}
		error = true;
		return result;
	}
	
 
    private LinkedList<CommandReply> pollResult(SageSingleCell.ServerTask server, int sequence) 
    		throws JSONException, IOException, URISyntaxException, SageInterruptedException {
		LinkedList<CommandReply> result = new LinkedList<CommandReply>();
		try {
			Thread.sleep(SLEEP_BEFORE_TRY);
		} catch (InterruptedException e) {
			Log.e(TAG, e.getLocalizedMessage());
			return result;
		}    	
    	HttpResponse response = server.pollOutput(this, sequence);
        error = (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK);
        HttpEntity entity = response.getEntity();
        InputStream outputStream = entity.getContent();
        String output = SageSingleCell.streamToString(outputStream);
        outputStream.close();

        //output = output.substring(7, output.length()-2);  // strip out "jQuery("...")"
        System.out.println("output = " + output);
        JSONObject outputJSON = new JSONObject(output);
        if (!outputJSON.has("content"))
        	return result;
       	JSONArray content = outputJSON.getJSONArray("content"); 
		for (int i=0; i<content.length(); i++) {
			JSONObject obj = content.getJSONObject(i);
			CommandReply command = CommandReply.parse(obj);
			result.add(command);
			if (command instanceof DataFile) {
				DataFile file = (DataFile)command;
				file.downloadFile(server);
			} else if (command instanceof HtmlFiles) {
				HtmlFiles file = (HtmlFiles)command;
				file.downloadFile(server);
			}
		}        
		return result;
    }	
    	 */

	public String toLongString() {
		JSONObject json;
		try {
			json = toJSON();
		} catch (JSONException e) {
			return e.getLocalizedMessage();
		}
		JSONWriter writer = new JSONWriter();
		writer.write(json.toString());
		//try {
		//	json.write(writer);
		//} catch (JSONException e) {
		//	return e.getLocalizedMessage();
		//}
		StringBuffer str = writer.getBuffer();
		return str.toString();
	}

	
}
