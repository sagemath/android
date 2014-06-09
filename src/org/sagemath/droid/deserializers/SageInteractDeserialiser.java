package org.sagemath.droid.deserializers;

import com.google.gson.*;
import org.sagemath.droid.models.InteractReply.InteractControl;
import org.sagemath.droid.models.InteractReply.SageInteract;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Haven
 */
public class SageInteractDeserialiser implements JsonDeserializer<SageInteract> {
    private static final String TAG = "SageDroid:Deserialiser";
    private Gson gson = new Gson();

    private static final String KEY_CONTROLS = "controls";
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
        for (Map.Entry<String, JsonElement> keys : control.getAsJsonObject().entrySet()) {
            varNames.add(keys.getKey());
        }

        //Iterate through the keys and deserialize
        for(String key:varNames) {
            InteractControl interactControl = context.deserialize(control.getAsJsonObject().get(key),
                    InteractControl.class);
            //Associate this control with the relevant key
            interactControl.setVarNames(key);
            controls.add(interactControl);

        }


        //Log.i(TAG, "Got InteractControl " + gson.toJson(interactControl));

        final SageInteract interact = new SageInteract();
        interact.setControls(controls);

        return interact;
    }
}
