package org.sagemath.droid.deserializers;

import com.google.gson.*;
import org.sagemath.droid.models.InteractReply.InteractData;
import org.sagemath.droid.models.InteractReply.SageInteract;

import java.lang.reflect.Type;

/**
 * Created by Haven on 30-05-2014.
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
