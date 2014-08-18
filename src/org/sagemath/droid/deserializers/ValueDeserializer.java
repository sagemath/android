package org.sagemath.droid.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.sagemath.droid.models.gson.Values;

import java.lang.reflect.Type;

/**
 * Deserializer for {@link org.sagemath.droid.models.gson.Values}
 *
 * <p>This Deserializer required is due to the fact that the key "values" can have value either a JSONArray
 * or an int</p>
 *
 * @author Nikhil Peter Raj
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
