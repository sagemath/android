package org.sagemath.droid.models;


public class Header {

    private String msg_id;
    private String username = ""; //Default
    private String session;
    private String msg_type;
    private String date;

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

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
