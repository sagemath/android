package org.sagemath.singlecellserver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Roughly, a CommandOutput is any JSON object that has a "output_block" field. These are intended 
 * to be displayed on the screen.
 * 
 * @author vbraun
 *
 */
public class CommandOutput extends CommandReply {
	private final static String TAG = "CommandOutput";
	
	private String output_block;
	
	protected CommandOutput(JSONObject json) throws JSONException {
		super(json);
		output_block = json.get("output_block").toString();
//		prettyPrint();
//		System.out.println("block = " + output_block);
	}

	public boolean containsOutput() {
		return true;
	}
	
	public String outputBlock() {
		return output_block;
	}
	
	
}
