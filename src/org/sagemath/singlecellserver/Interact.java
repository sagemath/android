package org.sagemath.singlecellserver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Interact extends CommandOutput {
	private final static String TAG = "Interact";
	
	private final String id;
	protected JSONObject controls;
	protected JSONArray layout;
	
	
	protected Interact(JSONObject json) throws JSONException {
		super(json);
		Log.i(TAG, "Created a new Interact!");
		JSONObject interact = json.getJSONObject("content").getJSONObject("data").getJSONObject("application/sage-interact");
		id = interact.getString("new_interact_id");
		controls = interact.getJSONObject("controls");
		layout = interact.getJSONArray("layout");
	}

	public long extendTimeOut() {
		return 60 * 1000;
	}
	
	public boolean isInteract() {
		return true;
	}
	
	public String getID() {
		return id;
	}
	
	public String toString() {
		return "Prepare interact id=" + getID();
	}
	
	public JSONObject getControls() {
		return controls;
	}
	
	public JSONArray getLayout() {
		return layout;
	}

}