package org.sagemath.droid.utils;

import android.net.Uri;

import static org.sagemath.droid.constants.StringConstants.BASE_SERVER_URL;
import static org.sagemath.droid.constants.StringConstants.PATH_KERNEL;
import static org.sagemath.droid.constants.StringConstants.SCHEME_HTTP;

/**
 * @author Haven
 */
public class UrlUtils {

    public static String getKernelURL() {
        Uri.Builder builder = new Uri.Builder();
        builder
                .scheme(SCHEME_HTTP)
                .authority(BASE_SERVER_URL)
                .appendPath(PATH_KERNEL)
                .build();

        return builder.toString();
    }

}
