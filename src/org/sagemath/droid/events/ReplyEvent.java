package org.sagemath.droid.events;

import org.sagemath.droid.models.gson.BaseReply;

/**
 * Created by Haven on 12-07-2014.
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
