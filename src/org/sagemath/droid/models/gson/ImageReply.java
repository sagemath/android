package org.sagemath.droid.models.gson;

import android.net.Uri;
import android.util.Log;
import com.google.gson.annotations.SerializedName;
import org.sagemath.droid.utils.UrlUtils;

/**
 * @author Nikhil Peter Raj
 *         Reply which contains an image name
 */
public class ImageReply extends BaseReply {
    private static final String TAG = "SageDroid:ImageReply";

    private static final String PATH_FILE = "files";
    private static final String SUFFIX_PNG = ".png";
    private static final String SUFFIX_JPG = ".jpg";
    private static final String SUFFIX_JPEG = ".jpeg";
    private static final String SUFFIX_SVG = ".svg";

    public static final String MIME_IMAGE_PNG = "image/png";
    public static final String MIME_IMAGE_SVG = "image/svg";

    public ImageReply() {
        super();
    }

    public String toString() {
        return gson.toJson(this);
    }

    private ImageContent content;

    public ImageContent getContent() {
        return content;
    }

    private transient String kernelID;

    public String getKernelID() {
        return kernelID;
    }

    public void setKernelID(String kernelID) {
        this.kernelID = kernelID;
    }

    public String getImageURL() {
        String kernelURL = UrlUtils.getInitialKernelURL();
        Uri imageUri = Uri.parse(kernelURL)
                .buildUpon()
                .appendPath(kernelID)
                .appendPath(PATH_FILE)
                .appendPath(getContent().getData().getImageFilename())
                .build();

        Log.i(TAG, "Returning image URL" + imageUri.toString());

        return imageUri.toString();
    }

    public String getImageMimeType() {
        String filename = getContent().getData().getImageFilename();
        if (filename.endsWith(SUFFIX_JPEG) || filename.endsWith(SUFFIX_JPG) || filename.endsWith(SUFFIX_PNG))
            return MIME_IMAGE_PNG;
        else if (filename.endsWith(SUFFIX_SVG))
            return MIME_IMAGE_SVG;
        else return null;
    }

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

}


