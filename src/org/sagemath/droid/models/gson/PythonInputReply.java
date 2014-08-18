package org.sagemath.droid.models.gson;

/**
 * Reply from the server representing Python input.
 * <p/>
 * <p>This class returns no data which must be displayed, instead it
 * is used to determine the server status, and accordingly perform required operations.</p>
 *
 * @author Nikhil Peter Raj
 */

public class PythonInputReply extends BaseReply {
    private static final String TAG = "SageDroid:PythonInputReply";

    private static final String STR_UPDATE_INTERACT = "sys._sage_.update_interact";

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

    //---UTILITY METHODS---
    public boolean isInteractUpdateReply() {
        if (getContent().getCode().contains(STR_UPDATE_INTERACT))
            return true;
        return false;
    }
}
