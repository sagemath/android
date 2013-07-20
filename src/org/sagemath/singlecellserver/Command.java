package org.sagemath.singlecellserver;

import java.util.UUID;

import org.json.JSONObject;
import android.util.Log;


/**
 * The base class for server communication. All derived classes should 
 * derive from CommandRequest and CommandReply, but not from Command
 * directly.
 * 
 * @author vbraun
 *
 */
public class Command {
	private final static String TAG = "Command";
	
	protected UUID session;
	protected UUID msg_id;
	protected UUID kernel_id;
	protected String kernel_url;
	
	protected Command() {
		this.session = UUID.randomUUID();
		this.msg_id = UUID.randomUUID();
		this.kernel_id =  UUID.randomUUID();
		this.kernel_url =  "";
	}
	
	protected Command(UUID session) {
		if (session == null) 
			this.session = UUID.randomUUID();
		else
			this.session = session;
		this.msg_id = UUID.randomUUID();
	}
	
	public String toShortString() {
		return toString();
	}
	
	public String toLongString() {
		return toString();
	}
	
	public String toString() {
		return "Command base class @" + Integer.toHexString(System.identityHashCode(this));
	}
	
	/** 
	 * Whether or not the command contains data that is supposed to be shown to the user
	 * @return boolean
	 */
	public boolean containsOutput() {
		Log.i(TAG, "Command.containsOutput is FALSE");
		return false;
	}
	
	protected static void prettyPrint(JSONObject json) {
		if (json == null) {
			System.out.println("null");
			return;
		}
		JSONWriter writer = new JSONWriter();
		writer.write(json.toString());
//		try {
//			json.write(writer);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
		StringBuffer str = writer.getBuffer();
		System.out.println(str);	
	}

	
}
