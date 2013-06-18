package org.sagemath.singlecellserver;

import java.util.LinkedList;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExecuteRequest extends CommandRequest {
	private final static String TAG = "ExecuteRequest";
	
	String input;
	boolean sage;
	
	public ExecuteRequest(String input, boolean sage, UUID session) {
		super(session);
		this.input = input;
		this.sage = sage;
	}
	
	public ExecuteRequest(String input) {
		super();
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
		JSONObject result = super.toJSON();
		result.put("parent_header", new JSONArray());
		result.put("msg_type", "execute_request");
		JSONObject content = new JSONObject();
		content.put("code", input);
		content.put("sage_mode", sage);
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
