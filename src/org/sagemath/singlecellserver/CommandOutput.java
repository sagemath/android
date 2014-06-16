package org.sagemath.singlecellserver;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.droid.models.BaseReply;

/**
 * Roughly, a CommandOutput is any JSON object that has a "output_block" field. These are intended
 * to be displayed on the screen.
 *
 * @author vbraun
 */
public class CommandOutput extends CommandReply {
    private final static String TAG = "SageDroid:CommandOutput";

    private String output_block;

    protected CommandOutput(JSONObject json) throws JSONException {
        super(json);
        // TODO: FIX THIS -- output_block is no longer a part of
        // the sage server's messages!
        output_block = "";
        //System.out.println("block = " + output_block);
    }

    protected CommandOutput(BaseReply reply) {
        super(reply);
        output_block = "";
    }

    protected CommandOutput() {

    }

    public boolean containsOutput() {
        //Log.i(TAG, "CommandOutput.containsOutput is TRUE");
        return true;
    }

    public String outputBlock() {

        Log.i(TAG, "Returning OutPutBlock" + output_block);
        return output_block;
    }

}
