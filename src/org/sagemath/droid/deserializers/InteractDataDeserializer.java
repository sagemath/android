package org.sagemath.droid.deserializers;

import com.google.gson.*;
import org.sagemath.droid.models.gson.InteractReply.InteractData;
import org.sagemath.droid.models.gson.InteractReply.SageInteract;

import java.lang.reflect.Type;

/**
 * Deserializer for {@link org.sagemath.droid.models.gson.InteractReply.InteractData}
 * @author Nikhil Peter Raj
 */
public class InteractDataDeserializer implements JsonDeserializer<InteractData> {

    private static final String KEY_INTERACT="application/sage-interact";
    private static final String KEY_DESC_TEXT="text/plain";

    @Override
    public InteractData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement interact = jsonObject.get(KEY_INTERACT);
        String descText = jsonObject.get(KEY_DESC_TEXT).getAsString();

        SageInteract sageInteract = context.deserialize(interact,SageInteract.class);

        final InteractData data = new InteractData();
        data.setDescText(descText);
        data.setInteract(sageInteract);

        return data;
    }
}
