package org.sagemath.droid.models;

import com.google.gson.annotations.SerializedName;

/**
 * @author Haven
 */
public class PythonOutputReply extends BaseReply {
    private PythonOutputContent content;

    public PythonOutputContent getContent() {
        return content;
    }

    public static class PythonOutputContent {

        private PythonOutputData data;
        private int execution_count;
        private MetaData metadata;

        public PythonOutputData getData() {
            return data;
        }

        public int getExecutionCount() {
            return execution_count;
        }
    }

    public static class PythonOutputData {

        @SerializedName("text/plain")
        private int outputValue;

        public int getOutputValue() {
            return outputValue;
        }
    }

    public static class MetaData{

    }


}
