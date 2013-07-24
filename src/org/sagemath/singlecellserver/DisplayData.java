package org.sagemath.singlecellserver;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class DisplayData extends CommandOutput {
	private final static String TAG = "DisplayData";
	
	private JSONObject data;
	protected String value, mime;
	
	protected DisplayData(JSONObject json) throws JSONException {
		super(json);
		Log.i(TAG, "Created new DisplayData!");
		data = json.getJSONObject("content").getJSONObject("data");
		mime = data.keys().next().toString();
		value = data.getString(mime);
		// prettyPrint(json);
	}

	public String toString() {
		return "Display data "+value;
	}

	public String getData() {
		return value;
	}
	
	public String getMime() {
		return mime;
	}

	public String toHTML() {
		if (mime.equals("text/html"))
			return value;
		if (mime.equals("text/plain"))
			return "<pre>"+value+"</pre>";
		//return null;
		return "Failed to handle mime in DisplayData (FIX THIS!)";
	}
	
}