package org.sagemath.droid.events;

import org.sagemath.droid.models.gson.BaseReply;

/**
 * @author Nikhil Peter Raj
 */
public class ReplyEvent {
    private BaseReply reply;

    public ReplyEvent(BaseReply reply) {
        this.reply = reply;
    }

    public BaseReply getReply() {
        return reply;
    }
}
