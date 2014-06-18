package org.sagemath.droid.models;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.sagemath.droid.constants.ExecutionState;
import org.sagemath.droid.constants.MessageType;
import org.sagemath.droid.deserializers.BaseReplyDeserializer;
import org.sagemath.droid.deserializers.InteractContentDeserializer;
import org.sagemath.droid.deserializers.InteractDataDeserializer;
import org.sagemath.droid.deserializers.SageInteractDeserializer;
import org.sagemath.singlecellserver.ServerReply;

/**
 * Base Reply from the Server
 * All *Reply classes should extend this
 *
 * @author Haven
 */
public class BaseReply {
    private static final String TAG = "SageDroid:BaseReply";

    protected Header header;
    protected Header parent_header;
    protected MetaData metadata;
    protected String msg_type;
    protected String msg_id;

    //To better determine type of reply received from the server
    protected transient int messageType;
    protected transient String mimeType;
    //The json, which is actually received, for use in toString()
    protected transient String jsonData;
    protected transient boolean isInteract;
    protected transient static BaseReply reply;
    protected transient static String jsonString;

    public transient Gson gson;

    public static class MetaData {

    }

    public BaseReply() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    //For nested classes wishing to use the base class gson
    protected Gson getGsonInstance() {
        return gson;
    }

    //---GETTERS & SETTERS---
    public boolean isInteract() {
        return isInteract;
    }

    public void setInteract(boolean isInteract) {
        this.isInteract = isInteract;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Header getParentHeader() {
        return parent_header;
    }

    public void setParentHeader(Header parent_header) {
        this.parent_header = parent_header;
    }

    public MetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(MetaData metadata) {
        this.metadata = metadata;
    }

    public String getStringMessageType() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getMessageID() {
        return msg_id;
    }

    public void setMessageID(String msg_id) {
        this.msg_id = msg_id;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    //---UTILITY METHODS---

    public boolean isReplyTo(Request request) {
        return (request != null) && getHeader().getSession().equals(request.getHeader().getSession());
    }

    public String toString() {
        return gson.toJson(this);
    }

    public static BaseReply parse(String jsonString) throws Exception {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(BaseReply.class, new BaseReplyDeserializer())
                .registerTypeAdapter(InteractReply.InteractContent.class, new InteractContentDeserializer())
                .registerTypeAdapter(InteractReply.InteractData.class, new InteractDataDeserializer())
                .registerTypeAdapter(InteractReply.SageInteract.class, new SageInteractDeserializer())
                .create();

        //Return the appropriate BaseReply
        BaseReply baseReply = gson.fromJson(jsonString, BaseReply.class);

        reply = baseReply;
        ServerReply.jsonString = jsonString;

        switch (baseReply.getMessageType()) {
            case MessageType.PYIN:
                Log.i(TAG, "Returning pyin");
                return gson.fromJson(jsonString, PythonInputReply.class);
            case MessageType.PYOUT:
                Log.i(TAG, "Returning pyout");
                return gson.fromJson(jsonString, PythonOutputReply.class);
            case MessageType.STATUS:
                Log.i(TAG, "Returning status");
                StatusReply statusReply = gson.fromJson(jsonString, StatusReply.class);
                saveStatus(statusReply);
                return statusReply;
            case MessageType.STREAM:
                Log.i(TAG, "Returning Stream");
                return gson.fromJson(jsonString, StreamReply.class);
            case MessageType.PYERR:
                Log.i(TAG, "Returning Pyerr");
                return gson.fromJson(jsonString, PythonErrorReply.class);
            case MessageType.EXECUTE_REPLY:
                Log.i(TAG, "Returning execute reply");
                return gson.fromJson(jsonString, SageExecuteReply.class);
            case MessageType.INTERACT:
                Log.i(TAG, "Returning Interact");
                return gson.fromJson(jsonString, InteractReply.class);
            case MessageType.SAGE_CLEAR:
                Log.i(TAG, "Returning Sage Clear");
                return gson.fromJson(jsonString, SageClearReply.class);
            case MessageType.HTML_FILES:
                Log.i(TAG, "Returning an HTML Reply");
                return gson.fromJson(jsonString, HtmlReply.class);
            case MessageType.IMAGE_FILENAME:
                Log.i(TAG, "Returning Image Reply");
                return gson.fromJson(jsonString, ImageReply.class);
            default:
                throw new Exception("Unknown Message Type");
        }
    }

    private static void saveStatus(StatusReply statusReply) {
        if (statusReply.getContent().getExecutionState() == ExecutionState.DEAD) {
            reply.getHeader().setSession(statusReply.getHeader().getSession());
            reply.getHeader().setMessageID(statusReply.getHeader().getMessageID());
        }
    }

}