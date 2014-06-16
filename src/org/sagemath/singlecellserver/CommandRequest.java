package org.sagemath.singlecellserver;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.droid.models.Header;
import org.sagemath.singlecellserver.SageSingleCell.ServerTask.postTask;

import java.util.UUID;

public class CommandRequest extends Command {
	private static final String TAG = "SageDroid:CommandRequest";
	
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

        Log.i(TAG,"Returning :"+result.toString(4));

		return result;
	}

    public Header getBaseHeader(){

        Header header = new Header();
        header.setSession(session.toString());
        header.setMessageID(msg_id.toString());

        return header;

    }
	
	
	protected void sendRequest(SageSingleCell.ServerTask server) {
		CommandReply reply;
		
		postTask initialPostTask = server.new postTask();
		try {
			Log.i(TAG, "CommandRequest.sendRequest() called");
            Log.i(TAG, "Executing Request:" + toJSON().toString(4));
			initialPostTask.execute(toJSON().toString());
			return;
		} catch (JSONException e) {
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
