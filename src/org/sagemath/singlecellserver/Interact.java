package org.sagemath.singlecellserver;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.droid.deserialisers.InteractContentDeserialiser;
import org.sagemath.droid.deserialisers.InteractDataDeserialiser;
import org.sagemath.droid.deserialisers.SageInteractDeserialiser;
import org.sagemath.droid.models.InteractReply;
import org.sagemath.droid.models.InteractReply.SageInteract;
import org.sagemath.droid.models.InteractReply.InteractContent;
import org.sagemath.droid.models.InteractReply.InteractData;

public class Interact extends CommandOutput {
    private final static String TAG = "SageDroid:Interact";

    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(InteractData.class, new InteractDataDeserialiser())
            .registerTypeAdapter(InteractContent.class, new InteractContentDeserialiser())
            .registerTypeAdapter(SageInteract.class, new SageInteractDeserialiser())
            .create();


    private final String id;
    protected JSONObject controls;
    protected JSONArray layout;


    protected Interact(JSONObject json) throws JSONException {
        super(json);

        Log.i(TAG, "Created a new Interact!" + json.toString(1));
        InteractReply reply = gson.fromJson(json.toString(), InteractReply.class);
        Log.i(TAG, "GSON Interact: " + gson.toJson(reply));
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

    public JSONArray getLayout() {
        return layout;
    }

}