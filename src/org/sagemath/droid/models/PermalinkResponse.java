package org.sagemath.droid.models;

import android.net.Uri;
import org.sagemath.droid.utils.UrlUtils;

/**
 * @author Haven
 */
public class PermalinkResponse {

    private String query;
    private String zip;

    public String getQueryID() {
        return query;
    }

    public String getZip() {
        return zip;
    }

    public String getQueryURL() {
        Uri queryUri = Uri.parse(UrlUtils.getPermalinkURL())
                .buildUpon()
                .appendQueryParameter("q", getQueryID())
                .build();

        return queryUri.toString();
    }

    public Uri getZipURL(Uri serverUri) {
        return serverUri.buildUpon()
                .appendQueryParameter("z", getQueryID())
                .build();
    }
}
