package org.sagemath.singlecellserver;

import org.json.JSONException;
import org.json.JSONObject;

public class Traceback extends CommandOutput {
	private final static String TAG = "Traceback";

	JSONObject content;
	String ename;
	String evalue;
	
	protected Traceback(JSONObject json) throws JSONException {
		super(json);
		// TODO Auto-generated constructor stub
		content = json.getJSONObject("content");
		ename = content.getString("ename");
		evalue = content.getString("evalue");
	}

	public String toString() {
		return ename+": "+evalue;
	}
	
	public String toShortString() {
		return "Traceback "+ename;
	}
	
}

