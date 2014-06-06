package org.sagemath.droid.deserialisers;

import com.google.gson.*;
import org.sagemath.droid.constants.MessageType;
import org.sagemath.droid.models.BaseReply;

import java.lang.reflect.Type;

/**
 * @author Haven
 */
public class BaseReplyDeserialiser implements JsonDeserializer<BaseReply> {

    public static final String STR_EXECUTE_REQUEST = "execute_request";
    public static final String STR_EXECUTE_REPLY = "execute_reply";
    public static final String STR_STREAM = "stream";
    public static final String STR_STATUS = "status";
    public static final String STR_PYIN = "pyin";
    public static final String STR_PYOUT = "pyout";
    public static final String STR_PYERR = "pyerr";
    public static final String STR_DISPLAY_DATA = "display_data";
    public static final String STR_TEXT_FILENAME = "text/filename";
    public static final String STR_IMAGE_FILENAME = "text/image-filename";
    public static final String STR_INTERACT = "application/sage-interact";
    public static final String STR_EXTENSION = "extension";

    public static final String KEY_CONTENT = "content";
    public static final String KEY_DATA = "data";

    @Override
    public BaseReply deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject base = json.getAsJsonObject();

        //Deserialise normally, messageType will be skipped since it is transient
        BaseReply reply = context.deserialize(json, BaseReply.class);

        //We parse it as int so it is easier to evaluate in a switch case.
        if (base.has(STR_PYIN)) {
            reply.setMessageType(MessageType.PYIN);
        } else if (base.has(STR_PYOUT))
            reply.setMessageType(MessageType.PYOUT);
        else if (base.has(STR_PYERR))
            reply.setMessageType(MessageType.PYERR);
        else if (base.has(STR_STATUS))
            reply.setMessageType(MessageType.STATUS);
        else if (base.has(STR_STREAM))
            reply.setMessageType(MessageType.STREAM);
        else if (base.has(STR_EXECUTE_REPLY))
            reply.setMessageType(MessageType.EXECUTE_REPLY);
        else if (base.has(STR_DISPLAY_DATA)) {
            JsonObject data = base.getAsJsonObject(KEY_CONTENT).getAsJsonObject(KEY_DATA);
            if (data.has(STR_TEXT_FILENAME))
                reply.setMessageType(MessageType.TEXT_FILENAME);
            else if (data.has(STR_IMAGE_FILENAME))
                reply.setMessageType(MessageType.TEXT_IMAGE_FILENAME);
            else if (data.has(STR_INTERACT))
                reply.setMessageType(MessageType.INTERACT);
        }

        return reply;

    }
}
