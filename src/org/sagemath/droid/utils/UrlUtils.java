package org.sagemath.droid.utils;

import android.net.Uri;
import android.util.Log;

import static org.sagemath.droid.constants.StringConstants.*;

/**
 * @author Haven
 */
public class UrlUtils {

    private static final String TAG = "SageDroid:UrlUtils";

    public static String getInitialKernelURL() {
        Uri.Builder builder = new Uri.Builder();
        builder
                .scheme(SCHEME_HTTP)
                .authority(BASE_SERVER_URL)
                .appendPath(PATH_KERNEL)
                .build();

        Log.i(TAG, "Initial Kernal URL: " + builder.toString());

        return builder.toString();
    }

    public static String getPermalinkURL() {
        Uri.Builder builder = new Uri.Builder();
        builder
                .scheme(SCHEME_HTTPS)
                .authority(BASE_SERVER_URL)
                .appendPath(PATH_PERMALINK)
                .build();

        Log.i(TAG,"Permalink URL: "+builder.toString());

        return builder.toString();
    }

    public static String getShellURL(String kernel_id, String webSocketUrl) {
        Uri shellURL = Uri.parse(webSocketUrl).buildUpon()
                .appendPath(PATH_KERNEL)
                .appendPath(kernel_id)
                .appendPath(PATH_SHELL)
                .build();

        Log.i(TAG, "Shell URL: " + shellURL.toString());

        return shellURL.toString();
    }

    public static String getIoPubURL(String kernel_id, String webSocketUrl) {
        Uri ioPubURL = Uri.parse(webSocketUrl).buildUpon()
                .appendPath(PATH_KERNEL)
                .appendPath(kernel_id)
                .appendPath(PATH_IOPUB)
                .build();

        Log.i(TAG, "IOPub URL: " + ioPubURL.toString());

        return ioPubURL.toString();
    }

}
