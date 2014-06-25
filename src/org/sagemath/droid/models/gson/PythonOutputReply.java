package org.sagemath.droid.models.gson;

import com.google.gson.annotations.SerializedName;

/**
 * @author Haven
 */
public class PythonOutputReply extends BaseReply {
    private PythonOutputContent content;

    public PythonOutputContent getContent() {
        return content;
    }

    public PythonOutputReply(){
        super();
    }

    public String toString() {
        return gson.toJson(this);
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
        private String outputValue;

        public String getOutputValue() {
            return outputValue;
        }
    }

    public static class MetaData {

    }


}
