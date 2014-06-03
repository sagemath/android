package org.sagemath.singlecellserver;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * The base class for a server reply
 *
 * @author vbraun
 */
public class CommandReply extends Command {
    private final static String TAG = "SageDroid:CommandReply";

    private JSONObject json;

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

    protected CommandReply(CommandRequest request) {
        session = request.session;
        msg_id = request.msg_id;
    }

    public String toString() {
        return json.toString();
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
        Log.i(TAG, "Received CommandReply"+json.toString(4));
        JSONObject header = json.getJSONObject("header");
        String msg_type = header.getString("msg_type");
        Log.i(TAG, "msg_type = " + msg_type);
        JSONObject content = json.getJSONObject("content");
        Log.i(TAG, "content = " + content.toString());
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

}
