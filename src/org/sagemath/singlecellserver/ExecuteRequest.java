package org.sagemath.singlecellserver;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class ExecuteRequest extends CommandRequest {
	private final static String TAG = "SageDroid:ExecuteRequest";
	
	String input;
	boolean sage;
	
	public ExecuteRequest(String input, boolean sage, UUID session) {
		super(session);
		Log.i(TAG, "ExecuteRequest called with input " + input);
		this.input = input;
		this.sage = sage;
	}
	
	public ExecuteRequest(String input) {
		super();
		//Log.i(TAG, "Input is " + input);
		this.input = input;
		sage = true;
	}

	public String toString() {
		return "Request to execute >"+input+"<";
	}

	public String toShortString() {
		return "Request to execute";
	}
	
	public JSONObject toJSON() throws JSONException {
		//Log.i(TAG, "ExecuteRequest.toJSON() called");
		JSONObject result = super.toJSON();
		JSONObject header = result.getJSONObject("header");
		header.put("msg_type", "execute_request");
		
		JSONObject content = new JSONObject();
		JSONObject user_expressions = new JSONObject();
		user_expressions.put("_sagecell_files","sys._sage_.new_files()");

		content.put("user_expressions", user_expressions);
		content.put("silent", false);
		content.put("code", input);
		content.put("allow_stdin", false);
		content.put("user_variables", new JSONArray());

		result.put("metadata", new JSONObject());
		result.put("parent_header", new JSONObject());
		result.put("header", header);
		result.put("content", content);

        Log.i(TAG,"ExecuteRequest Returning:" + result.toString(4));
		
		return result;
	}
	
}
