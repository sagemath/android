package org.sagemath.droid.deserialisers;

import android.util.Log;
import com.google.gson.*;
import org.sagemath.droid.models.InteractControl;
import org.sagemath.droid.models.SageInteract;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by Haven on 30-05-2014.
 */
public class SageInteractDeserialiser implements JsonDeserializer<SageInteract> {
    private static final String TAG = "SageDroid:Deserialiser";
    private Gson gson = new Gson();

    private static final String KEY_CONTROLS = "controls";
    private String varName;

    @Override
    public SageInteract deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement control = jsonObject.get(KEY_CONTROLS);

        //Here we modify the model slightly and add the varName indirectly to our InteractControl
        //by obtaining it from the list of keys from the JSON and passing the

        //This way, we can continue to use Gson for the normal deserialization of
        //our InteractControl class
        for (Map.Entry<String, JsonElement> keys : control.getAsJsonObject().entrySet()) {
            varName = keys.getKey();
        }

        InteractControl interactControl = context.deserialize(control.getAsJsonObject().get(varName),
                InteractControl.class);
        interactControl.setVarName(varName);

        Log.i(TAG, "Got InteractControl " + gson.toJson(interactControl));

        final SageInteract interact = new SageInteract();
        interact.setControls(interactControl);

        return interact;
    }
}
