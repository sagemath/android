package org.sagemath.singlecellserver;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.droid.constants.ExecutionState;
import org.sagemath.droid.constants.MessageType;
import org.sagemath.droid.deserializers.BaseReplyDeserializer;
import org.sagemath.droid.deserializers.InteractContentDeserialiser;
import org.sagemath.droid.deserializers.InteractDataDeserialiser;
import org.sagemath.droid.deserializers.SageInteractDeserialiser;
import org.sagemath.droid.models.*;
import org.sagemath.droid.models.InteractReply.InteractContent;
import org.sagemath.droid.models.InteractReply.InteractData;
import org.sagemath.droid.models.InteractReply.SageInteract;

import java.util.UUID;

/**
 * The base class for a server reply
 *
 * @author vbraun
 */
public class CommandReply extends Command {
    private final static String TAG = "SageDroid:CommandReply";

    private JSONObject json;
    private BaseReply reply;
    private Gson gson = new Gson();


    protected CommandReply(JSONObject json) throws JSONException {
        this.json = json;
        JSONObject parent_header = json.getJSONObject("parent_header");
        // TODO Make this clean! It makes no sense at all from a design perspective.
        // Maybe just get rid of it entirely.
        if (this instanceof Status) {
            JSONObject content = json.getJSONObject("content");
            if (content.getString("execution_state").equals("dead")) {
                // To prevent JSONExceptions of iopub's dead execution state not
                // having a session or msg_id.
                // msg_id doesn't matter for iopub when execution state is dead...
                // Need to preserve session in case of update_interact.
                JSONObject header = json.getJSONObject("header");
                session = UUID.fromString(header.getString("session"));
                msg_id = UUID.fromString(header.getString("msg_id"));
            }
        } else {
            session = UUID.fromString(parent_header.getString("session"));
            msg_id = UUID.fromString(parent_header.getString("msg_id"));
        }
    }

    protected CommandReply(BaseReply reply) {
        this.reply = reply;
        if (reply instanceof StatusReply) {
            StatusReply statusReply = (StatusReply) reply;

            if (statusReply.getContent().getExecutionState() == ExecutionState.DEAD) {
                session = UUID.fromString(reply.getHeader().getSession());
                msg_id = UUID.fromString(reply.getHeader().getMessageID());
            } else {
                session = UUID.fromString(reply.getParentHeader().getSession());
                msg_id = UUID.fromString(reply.getParentHeader().getSession());
            }
        }
    }

    protected CommandReply() {

    }

    protected CommandReply(CommandRequest request) {
        session = request.session;
        msg_id = request.msg_id;
    }

    public String toString() {
        if (json != null)
            return json.toString();
        else return reply.getJsonData();
    }

    public void prettyPrint() {
        prettyPrint(json);
    }

    /**
     * Extend the HTTP timetout receive timeout. This is for interacts to extend the timeout.
     *
     * @return milliseconds
     */
    public long extendTimeOut() {
        return 0;
    }

    public boolean isInteract() {
        return false;
    }

    /**
     * Whether to keep polling for more results after receiving this message
     *
     * @return
     */
    public boolean terminateServerConnection() {
        return false;
    }

    /**
     * Turn a received JSONObject into the corresponding Command object
     *
     * @return a new CommandReply or derived class
     */
    protected static CommandReply parse(JSONObject json) throws JSONException {
        Log.i(TAG, "Received CommandReply" + json.toString(4));

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(BaseReply.class, new BaseReplyDeserializer())
                .create();

        BaseReply reply = gson.fromJson(json.toString(), BaseReply.class);

        Log.i(TAG, "Received BaseReply: " + gson.toJson(reply));
        Log.i(TAG, "Message Type:" + reply.getMessageType());

        JSONObject header = json.getJSONObject("header");
        String msg_type = header.getString("msg_type");
        Log.i(TAG, "JSON PARSE msg_type = " + msg_type);
        JSONObject content = json.getJSONObject("content");
        Log.i(TAG, "JSON PARSE content = " + content.toString());
        if (msg_type.equals("pyout"))
            return new PythonOutput(json);
        else if (msg_type.equals("status")) {
            return new Status(json);
        } else if (msg_type.equals("pyin")) {
            return new PythonInput(json);
        } else if (msg_type.equals("display_data")) {
            JSONObject data = json.getJSONObject("content").getJSONObject("data");
            if (data.has("text/filename"))
                return new DataFile(json);
            else if (data.has("text/image-filename"))
                return new DataFile(json);
            else if (data.has("application/sage-interact"))
                return new Interact(json);
            else
                return new DisplayData(json);
        } else if (msg_type.equals("stream"))
            return new ResultStream(json);
        else if (msg_type.equals("pyerr"))
            return new Traceback(json);
        else if (msg_type.equals("execute_reply")) {
            return new ExecuteReply(json);
        } else if (msg_type.equals("extension")) {
            String ext_msg_type = content.getString("msg_type");
            if (ext_msg_type.equals("session_end"))
                return new SessionEnd(json);
            if (ext_msg_type.equals("files"))
                return new HtmlFiles(json);
            if (ext_msg_type.equals("interact_prepare"))
                return new Interact(json);
        }
        throw new JSONException("Unknown msg_type");
    }

    protected static CommandReply parse(String jsonString) throws Exception {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(BaseReply.class, new BaseReplyDeserializer())
                .registerTypeAdapter(InteractContent.class, new InteractContentDeserialiser())
                .registerTypeAdapter(InteractData.class, new InteractDataDeserialiser())
                .registerTypeAdapter(SageInteract.class, new SageInteractDeserialiser())
                .create();

        //Return the appropriate CommandReply
        BaseReply baseReply = gson.fromJson(jsonString, BaseReply.class);

        switch (baseReply.getMessageType()) {
            case MessageType.PYIN:
                Log.i(TAG, "Returning pyin");
                return new PythonInput(gson.fromJson(jsonString, PythonInputReply.class));
            case MessageType.PYOUT:
                Log.i(TAG, "Returning pyout");
                return new PythonOutput(gson.fromJson(jsonString, PythonOutputReply.class));
            case MessageType.STATUS:
                Log.i(TAG, "Returning status");
                StatusReply status = gson.fromJson(jsonString, StatusReply.class);
                return new Status(status);
            case MessageType.STREAM:
                Log.i(TAG, "Returning Stream");
                return new ResultStream(gson.fromJson(jsonString, StreamReply.class));
            case MessageType.PYERR:
                Log.i(TAG, "Returning Pyerr");
                return new Traceback(gson.fromJson(jsonString, PythonErrorReply.class));
            case MessageType.EXECUTE_REPLY:
                Log.i(TAG, "Returning execute reply");
                return new ExecuteReply(gson.fromJson(jsonString, SageExecuteReply.class));
            case MessageType.INTERACT:
                Log.i(TAG, "Returning Interact");
                return new Interact(gson.fromJson(jsonString, InteractReply.class));
            default:
                throw new Exception("Unknown Message Type");
        }
    }


    public String toLongString() {
        if (json == null)
            return "null";
        //JSONWriter writer = new JSONWriter();
        //writer.write(json.toString());
        //StringBuffer str = writer.getBuffer();
        //return str.toString();
        String returnJson = "";
        try {
            returnJson = json.toString(4);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return returnJson;
        }
    }


    /**
     * Whether the reply is a reply to the given request
     *
     * @param request
     * @return boolean
     */
    public boolean isReplyTo(CommandRequest request) {
        return (request != null) && session.equals(request.session);
    }

    public boolean isReplyTo(Request request) {
        return (request != null) && session.equals(request.getHeader().getSession());
    }

}
