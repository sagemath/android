package org.sagemath.droid.deserializers;

import android.util.Log;
import com.google.gson.*;
import org.sagemath.droid.constants.MessageType;
import org.sagemath.droid.models.gson.BaseReply;

import java.lang.reflect.Type;

/**
 *
 * The Base Deserializer, which is used to obtain a {@link org.sagemath.droid.models.gson.BaseReply}
 *
 * @author Nikhil Peter Raj
 */
public class BaseReplyDeserializer implements JsonDeserializer<BaseReply> {

    private static final String TAG = "SageDroid:BaseReplyDeserializer";

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
    public static final String STR_SAGE_CLEAR = "application/sage-clear";
    public static final String STR_EXTENSION = "extension";

    private static final String MIME_TEXT_PLAIN = "text/plain";
    private static final String MIME_TEXT_HTML = "text/html";

    public static final String KEY_CONTENT = "content";
    public static final String KEY_DATA = "data";

    //TODO Should Probably use the TypeAdapterFactory way to do this, this is more hacky
    //Still haven't found a suitable way to do so.

    @Override
    public BaseReply deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject base = json.getAsJsonObject();

        Log.i(TAG, base.toString());

        //Deserialise normally, messageType will be skipped since it is transient

        BaseReply reply = new Gson().fromJson(json, BaseReply.class);

        Log.i(TAG, "Deserializing BaseReply " + new Gson().toJson(reply));

        String messageType = reply.getHeader().getStringMessageType();

        Log.i(TAG, "Message Type is " + messageType);
        //We parse it as int so it is easier to evaluate in a switch case.
        if (messageType.equalsIgnoreCase(STR_PYIN)) {
            reply.setMessageType(MessageType.PYIN);
        } else if (messageType.equalsIgnoreCase(STR_PYOUT))
            reply.setMessageType(MessageType.PYOUT);
        else if (messageType.equalsIgnoreCase(STR_PYERR))
            reply.setMessageType(MessageType.PYERR);
        else if (messageType.equalsIgnoreCase(STR_STATUS))
            reply.setMessageType(MessageType.STATUS);
        else if (messageType.equalsIgnoreCase(STR_STREAM))
            reply.setMessageType(MessageType.STREAM);
        else if (messageType.equalsIgnoreCase(STR_EXECUTE_REPLY))
            reply.setMessageType(MessageType.EXECUTE_REPLY);
        else if (messageType.equalsIgnoreCase(STR_DISPLAY_DATA)) {
            JsonObject data = base.getAsJsonObject(KEY_CONTENT).getAsJsonObject(KEY_DATA);
            if (data.has(STR_TEXT_FILENAME))
                reply.setMessageType(MessageType.TEXT_FILENAME);
            else if (data.has(STR_IMAGE_FILENAME))
                reply.setMessageType(MessageType.IMAGE_FILENAME);
            else if (data.has(STR_INTERACT))
                reply.setMessageType(MessageType.INTERACT);
            else if (data.has(STR_SAGE_CLEAR))
                reply.setMessageType(MessageType.SAGE_CLEAR);
            else if (data.has(MIME_TEXT_HTML)) {
                reply.setMessageType(MessageType.HTML_FILES);
                reply.setMimeType(MIME_TEXT_HTML);
            } else if (data.has(MIME_TEXT_PLAIN))
                reply.setMimeType(MIME_TEXT_PLAIN);
        }

        reply.setJsonData(base.toString());

        return reply;

    }
}
