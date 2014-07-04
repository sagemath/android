package org.sagemath.droid.deserializers;

import android.util.Log;
import com.google.gson.*;
import org.sagemath.droid.models.gson.InteractReply.InteractControl;
import org.sagemath.droid.models.gson.InteractReply.SageInteract;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Haven
 */
public class SageInteractDeserializer implements JsonDeserializer<SageInteract> {
    private static final String TAG = "SageDroid:SageInteractDeserializer";
    private Gson gson = new Gson();

    private static final String KEY_CONTROLS = "controls";
    private static final String KEY_NEW_INTERACT_ID = "new_interact_id";
    private ArrayList<String> varNames;
    private ArrayList<InteractControl> controls;

    @Override
    public SageInteract deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement control = jsonObject.get(KEY_CONTROLS);
        varNames = new ArrayList<String>();
        controls = new ArrayList<InteractControl>();
        //Here we modify the model slightly and add the varName indirectly to our InteractControl
        //by obtaining it from the list of keys from the JSON and passing the

        //This way, we can continue to use Gson for the normal deserialization of
        //our InteractControl class

        //Get all the possible keys for InteractControl

        Log.i(TAG, "Got Control" + control.toString());

        Log.i(TAG, "No. of controls " + control.getAsJsonObject().entrySet().size());

        //Iterate through keys and deserialize
        for (Map.Entry<String, JsonElement> keys : control.getAsJsonObject().entrySet()) {
            varNames.add(keys.getKey());
            Log.i(TAG, "Got Key: " + keys.getKey());
            InteractControl interactControl = context.deserialize(keys.getValue(), InteractControl.class);
            Log.i(TAG, "Deserializing: " + interactControl.toString());
            interactControl.setVarName(keys.getKey());
            controls.add(interactControl);
        }

        //TODO Find way to add the omitted data
        final SageInteract interact = new SageInteract();
        interact.setControls(controls);
        interact.setNewInteractID(jsonObject.getAsJsonPrimitive(KEY_NEW_INTERACT_ID).getAsString());

        return interact;
    }
}
