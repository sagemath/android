package org.sagemath.singlecellserver;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class DisplayData extends CommandOutput {
	private final static String TAG = "SageDroid:DisplayData";
	
	private JSONObject data;
	protected String value, mime;
	
	protected DisplayData(JSONObject json) throws JSONException {
		super(json);
		data = json.getJSONObject("content").getJSONObject("data");
		mime = data.keys().next().toString();
		value = data.getString(mime);
		Log.i(TAG, "DisplayData value is: " + value);
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
		return "";
	}
	
}