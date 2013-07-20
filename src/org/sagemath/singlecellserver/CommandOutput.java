package org.sagemath.singlecellserver;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

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
		// TODO: FIX THIS -- output_block is no longer a part of 
		// the sage server's messages!
		output_block = "Why does this even exist anymore?";
//		prettyPrint();
//		System.out.println("block = " + output_block);
	}

	public boolean containsOutput() {
		Log.i(TAG, "CommandOutput.containsOutput is TRUE");
		return true;
	}
	
	public String outputBlock() {
		return output_block;
	}
	
	
}
