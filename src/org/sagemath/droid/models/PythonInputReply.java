package org.sagemath.droid.models;

/**
 * For pyin messages
 */

public class PythonInputReply extends BaseReply {
    private PythonInputContent content;

    public PythonInputReply() {
        super();
    }

    public String toString() {
        return gson.toJson(this);
    }

    public PythonInputContent getContent() {
        return content;
    }

    public static class PythonInputContent {
        private int execution_count;
        private String code;

        public int getExecutionCount() {
            return execution_count;
        }

        public String getCode() {
            return code;
        }
    }
}
