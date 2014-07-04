package org.sagemath.droid.models.gson;

import org.sagemath.droid.constants.MessageType;

import java.util.UUID;

public class Header {

    //---STATIC VARIABLES---
    public static final String STR_EXECUTE_REQUEST = "execute_request";
    public static final String STR_EXECUTE_REPLY = "execute_reply";
    public static final String STR_STREAM = "stream";
    public static final String STR_STATUS = "status";
    public static final String STR_PYIN = "pyin";
    public static final String STR_PYOUT = "pyout";
    public static final String STR_PYERR = "pyerr";
    public static final String STR_DISPLAY_DATA = "display_data";
    public static final String STR_EXTENSION = "extension";
    public static final String STR_INTERACT_PREPARE = "interact_prepare";
    public static final String STR_HTML_FILES = "html_files";

    //---CLASS VARIABLES---
    private String msg_id;
    private String username = ""; //Default
    private String session;
    private String msg_type;
    private String date;

    //--SETTERS AND GETTERS
    public String getMessageID() {
        return msg_id;
    }

    public void setMessageID(String msg_id) {
        this.msg_id = msg_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    //Possibly redundant, now that we check from BaseReply directly
    public int getMessageType() {
        if (msg_type.equalsIgnoreCase(STR_EXECUTE_REPLY))
            return MessageType.EXECUTE_REPLY;
        else if (msg_type.equalsIgnoreCase(STR_STATUS))
            return MessageType.STATUS;
        else if (msg_type.equalsIgnoreCase(STR_STREAM))
            return MessageType.STREAM;
        else if (msg_type.equalsIgnoreCase(STR_EXECUTE_REQUEST))
            return MessageType.EXECUTE_REQUEST;
        else if (msg_type.equalsIgnoreCase(STR_DISPLAY_DATA))
            return MessageType.DISPLAY_DATA;
        else if (msg_type.equalsIgnoreCase(STR_PYIN))
            return MessageType.PYIN;
        else if (msg_type.equalsIgnoreCase(STR_PYOUT))
            return MessageType.PYOUT;
        else if (msg_type.equalsIgnoreCase(STR_PYERR))
            return MessageType.PYERR;
        else if (msg_type.equalsIgnoreCase(STR_EXTENSION))
            return MessageType.EXTENSION;
        else return MessageType.ERROR;
    }

    public String getStringMessageType() {
        return msg_type;
    }

    public void setMessageType(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    //UTILITY METHODS

    public void init() {
        setSession(UUID.randomUUID().toString());
        setMessageID(UUID.randomUUID().toString());
    }

    public void init(String session) {
        setSession(session);
        setMessageID(UUID.randomUUID().toString());
    }

    public void init(String session, String msg_id) {
        setSession(session);
        setMessageID(msg_id);
    }

    public void init(String session, String msg_id, String username) {
        setSession(session);
        setMessageID(msg_id);
        setUsername(username);
    }
}
