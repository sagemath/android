package org.sagemath.droid.models;

import com.google.gson.annotations.SerializedName;

/**
 * @author Haven
 */
public class ImageReply extends BaseReply {

    private ImageContent content;

    public static class ImageContent {

        private ImageData data;

        public ImageData getData() {
            return data;
        }
    }

    public static class ImageData {

        @SerializedName("text/image-filename")
        private String imageFilename;

        @SerializedName("text/plain")
        private String descText;

        public String getImageFilename() {
            return imageFilename;
        }

        public String getDescText() {
            return descText;
        }

    }

    public ImageContent getContent() {
        return content;
    }
}


