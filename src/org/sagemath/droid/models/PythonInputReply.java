package org.sagemath.droid.models;

/**
 * For pyin messages
 */

public class PythonInputReply extends BaseReply {
    private PythonInputContent content;

    public PythonInputContent getContent() {
        return content;
    }

    public static class PythonInputContent {
        private int execution_count;
        private String code;
    }
}
