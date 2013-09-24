package org.sagemath.singlecellserver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.singlecellserver.SageSingleCell.SageInterruptedException;

import android.util.Log;

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
		//Log.i(TAG, "CommandRequest.toJSON() called");
		JSONObject header = new JSONObject();

		header.put("session", session.toString());
		header.put("msg_id", msg_id.toString());
		header.put("username","");
		JSONObject result = new JSONObject();
		result.put("header", header);
		return result;
	}
	
	
	protected void sendRequest(SageSingleCell.ServerTask server) {
		CommandReply reply;
		try {
			Log.i(TAG, "CommandRequest.sendRequest() called");
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

	public String toLongString() {
		JSONObject json;
		try {
			json = toJSON();
		} catch (JSONException e) {
			return e.getLocalizedMessage();
		}
		JSONWriter writer = new JSONWriter();
		writer.write(json.toString());
		StringBuffer str = writer.getBuffer();
		return str.toString();
	}

	
}
