package org.sagemath.singlecellserver;

import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.droid.models.PythonErrorReply;

public class Traceback extends CommandOutput {
    private final static String TAG = "SageDroid:Traceback";

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

    protected Traceback(PythonErrorReply reply) {
        super(reply);
        ename = reply.getContent().getEname();
        evalue = reply.getContent().getEvalue();
    }

    public String toString() {
        return ename + ": " + evalue;
    }

    public String toShortString() {
        return "Traceback " + ename;
    }

}

