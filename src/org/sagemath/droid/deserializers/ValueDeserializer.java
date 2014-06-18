package org.sagemath.droid.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.sagemath.droid.models.Values;

import java.lang.reflect.Type;

/**
 * @author Haven
 *         This Deserializer required is due to the fact that the key "values" can have value either a JSONArray
 *         or an int
 */
public class ValueDeserializer implements JsonDeserializer<Values> {

    @Override
    public Values deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        final Values values = new Values();

        if (json.isJsonArray()) {
            String[] vals = context.deserialize(json, String[].class);
            values.setValues(vals);
        } else if (json.isJsonPrimitive()) {
            values.setIntValue(json.getAsJsonPrimitive().getAsInt());
        }

        return values;

    }
}
