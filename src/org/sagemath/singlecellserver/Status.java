package org.sagemath.singlecellserver;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 *
 * Get the execution state of a status message.
 *
 */
public class Status extends CommandOutput {
	private final static String TAG = "SageDroid:Status";
	
	protected JSONObject content;	
	protected String execution_state;
	
	protected Status(JSONObject json) throws JSONException {
		super(json);
		content = json.getJSONObject("content");
		execution_state = content.getString("execution_state");
		Log.d(TAG, "State: " + execution_state);
	}

	public String toString() {
		return "Status: " + execution_state;
	}

	public String toShortString() {
		return "Stream output";
	}

	public String get() {
		return execution_state;
	}
	
}
