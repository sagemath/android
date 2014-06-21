package org.sagemath.droid.models;

/**
 * @author Haven
 */
public class SageExecuteReply extends BaseReply {

    private ExecuteReplyContent content;

    public ExecuteReplyContent getContent() {
        return content;
    }

    public static class ExecuteReplyContent {
        private String status;

        public String getStatus() {
            return status;
        }
    }
}
