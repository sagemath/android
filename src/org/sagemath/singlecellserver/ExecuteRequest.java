package org.sagemath.singlecellserver;

import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class ExecuteRequest extends CommandRequest {
	private final static String TAG = "ExecuteRequest";
	
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
		Log.i(TAG, "Input is " + input);
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
		Log.i(TAG, "ExecuteRequest.toJSON() called");
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

		//content.put("sage_mode", sage);
		
		result.put("metadata", new JSONObject());
		result.put("parent_header", new JSONObject());
		result.put("header", header);
		result.put("content", content);

		
		return result;
	}
	
	
	
//    	{"parent_header":{},
//    		"header":{
//    		   "msg_id":"1ec1b4b4-722e-42c7-997d-e4a9605f5056",
//    		   "session":"c11a0761-910e-4c8c-b94e-803a13e5859a"},
//    		"msg_type":"execute_request",
//    		"content":{"code":"code to execute",
//    		            "sage_mode":true}}

    

	
}
