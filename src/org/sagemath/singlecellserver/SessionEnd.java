package org.sagemath.singlecellserver;

import org.json.JSONException;
import org.json.JSONObject;

public class SessionEnd extends CommandReply {
	public final static String TAG = "SageDroid:SessionEnd";

	protected SessionEnd(JSONObject json) throws JSONException {
		super(json);
	}

	public String toString() {
		return "End of Sage session marker";
	}
		
	@Override
	public boolean terminateServerConnection() {
		return true;
	}
	
}
