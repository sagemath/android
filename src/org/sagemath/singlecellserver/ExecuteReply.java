package org.sagemath.singlecellserver;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.droid.models.SageExecuteReply;

public class  ExecuteReply extends CommandOutput {
    private final static String TAG = "SageDroid:ExecuteReply";

    private String status;

    protected ExecuteReply(JSONObject json) throws JSONException {
        super(json);
        Log.i(TAG, "Received ExecuteReply" + json.toString(4));
        JSONObject content = json.getJSONObject("content");
        status = content.getString("status");
    }

    protected ExecuteReply(SageExecuteReply reply) {
        super(reply);
        status = reply.getContent().getStatus();
    }

    public String toString() {
        return "Execute reply status = " + status;
    }

    public String getStatus() {
        return status;
    }

}
