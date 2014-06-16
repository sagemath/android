package org.sagemath.singlecellserver;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.droid.models.InteractReply;
import org.sagemath.droid.models.InteractReply.InteractControl;

public class Interact extends CommandOutput {
    private final static String TAG = "SageDroid:Interact";

    private String id;
    private InteractControl interactControl;
    protected JSONObject controls;
    protected JSONArray layout;

    protected Interact(InteractReply reply) {
        super(reply);
        id = reply.getContent().getData().getInteract().getNewInteractID();
        //interactControl = reply.getContent().getData().getInteract().getControls();
    }


    protected Interact(JSONObject json) throws JSONException {
        super(json);

        Log.i(TAG, "Created a new Interact!" + json.toString(1));
        //InteractReply reply = gson.fromJson(json.toString(), InteractReply.class);
        //Log.i(TAG, "GSON Interact: " + gson.toJson(reply));
        JSONObject interact = json.getJSONObject("content").getJSONObject("data").getJSONObject("application/sage-interact");
        id = interact.getString("new_interact_id");
        controls = interact.getJSONObject("controls");
        layout = interact.getJSONArray("layout");
    }

    public long extendTimeOut() {
        return 60 * 1000;
    }

    public boolean isInteract() {
        return true;
    }

    public String getID() {
        return id;
    }

    public String toString() {
        return "Prepare interact id=" + getID();
    }

    public JSONObject getControls() {
        return controls;
    }

    public InteractControl getInteractControl() {
        return interactControl;
    }

    public JSONArray getLayout() {
        return layout;
    }

}