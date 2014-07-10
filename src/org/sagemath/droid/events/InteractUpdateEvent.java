package org.sagemath.droid.events;

import org.sagemath.droid.models.gson.InteractReply;

/**
 * Created by Haven on 09-07-2014.
 */
public class InteractUpdateEvent {
    private InteractReply reply;
    private String varName;
    private Object value;

    public InteractUpdateEvent(InteractReply reply, String varName, Object value) {
        this.reply = reply;
        this.varName = varName;
        this.value = value;
    }

    public InteractReply getReply() {
        return reply;
    }

    public String getVarName() {
        return varName;
    }

    public Object getValue() {
        return value;
    }
}
