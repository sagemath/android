package org.sagemath.singlecellserver;

import org.json.JSONException;
import org.json.JSONObject;

public class ExecuteReply extends CommandOutput {
	private final static String TAG = "ExecuteReply"; 

	private String status;
	
	protected ExecuteReply(JSONObject json) throws JSONException {
		super(json);
		JSONObject content = json.getJSONObject("content");
		status = content.getString("status");
	}

	public String toString() {
		return "Execute reply status = "+status;
	}
	
	public String getStatus() {
		return status;
	}

}
