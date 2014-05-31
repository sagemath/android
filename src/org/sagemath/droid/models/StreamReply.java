package org.sagemath.droid.models;

/**
 * Created by Haven on 29-05-2014.
 */
public class StreamReply extends BaseReply {
    private StreamContent content;

    public StreamContent getContent() {
        return content;
    }

    public static class StreamContent {
        private String data;
        private String name;

        public String getData() {
            return data;
        }

        public String getName() {
            return name;
        }
    }
}
