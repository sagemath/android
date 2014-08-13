package org.sagemath.droid.events;

import org.sagemath.droid.models.gson.BaseReply;

/**
 * <p>The event which encapsulates a {@linkplain org.sagemath.droid.models.gson.BaseReply}</p>
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
