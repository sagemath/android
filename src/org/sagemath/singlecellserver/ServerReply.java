package org.sagemath.singlecellserver;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.sagemath.droid.constants.ExecutionState;
import org.sagemath.droid.constants.MessageType;
import org.sagemath.droid.deserializers.BaseReplyDeserializer;
import org.sagemath.droid.deserializers.InteractContentDeserialiser;
import org.sagemath.droid.deserializers.InteractDataDeserialiser;
import org.sagemath.droid.deserializers.SageInteractDeserialiser;
import org.sagemath.droid.models.*;

/**
 * @author Haven
 */
public class ServerReply extends BaseReply {
    private static final String TAG = "SageDroid:ServerReply";

    public static BaseReply reply;
    public static String jsonString;


    public static BaseReply parse(String jsonString) throws Exception {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(BaseReply.class, new BaseReplyDeserializer())
                .registerTypeAdapter(InteractReply.InteractContent.class, new InteractContentDeserialiser())
                .registerTypeAdapter(InteractReply.InteractData.class, new InteractDataDeserialiser())
                .registerTypeAdapter(InteractReply.SageInteract.class, new SageInteractDeserialiser())
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

    public static BaseReply getSavedStatus() {
        return reply;
    }

    public static String getSavedJsonString() {
        return jsonString;
    }

}
