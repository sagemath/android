package org.sagemath.singlecellserver;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


/**
 *
 * 
 * List the input of any pyin messages
 */
public class PythonInput extends CommandOutput {
	private final static String TAG = "PythonOutput";
	
	protected JSONObject content;
	protected String pyinput;
	
	protected PythonInput(JSONObject json) throws JSONException {
		super(json);
		Log.i(TAG, "PythonInput created!");
		content = json.getJSONObject("content");
		pyinput = content.getString("code");
	}

	public String toString() {
		return "Python input: "+ pyinput;
	}
	
	public String toShortString() {
		return "Python input";
	}

	//Encoding details not necessary for the input?
	/**
	 * Get an iterator for the possible encodings.
	 * 
	 * @return
	 */
	/*
	public Iterator<?> getEncodings() {
		return content.keys();
	}
	 */
	/**
	 * Get the output
	 * 
	 * @param encoding Which of possibly multiple representations to return
	 * @return The output in the chosen representation
	 * @throws JSONException
	 */
	/*
	public String get(String encoding) throws JSONException {
		return data.getString(encoding);
	}
*/
	
	/**
	 * Return a textual representation of the input
	 * 
	 * @return Text representation of the input;
	 */
	public String get() {
		return pyinput + "=";
	}
	
}
