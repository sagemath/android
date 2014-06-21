package org.sagemath.droid.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * @author Haven
 */
public class SageClearReply extends BaseReply {

    public SageClearContent content;

    public SageClearReply() {
        super();
    }

    public String toString() {
        return gson.toJson(this);
    }

    public SageClearContent getContent() {
        return content;
    }

    public static class SageClearContent {

        private SageClearData data;

        public SageClearData getData() {
            return data;
        }
    }

    public static class SageClearData {

        @SerializedName("application/sage-clear")
        private SageClear sageClear;

        @SerializedName("text/plain")
        private String plainText;

        public SageClear getSageClear() {
            return sageClear;
        }

        public String getPlainText() {
            return plainText;
        }
    }

    public static class SageClear {

        private ArrayList<String> changed;

        public ArrayList<String> getChanged() {
            return changed;
        }

        public void setChanged(ArrayList<String> changed) {
            this.changed = changed;
        }
    }
}
