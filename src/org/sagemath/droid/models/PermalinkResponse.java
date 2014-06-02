package org.sagemath.droid.models;

import android.net.Uri;

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

    public Uri getQueryUri(Uri serverUri) {
        return serverUri.buildUpon()
                .appendQueryParameter("q", getQueryID())
                .build();
    }

    public Uri getZipUri(Uri serverUri) {
        return serverUri.buildUpon()
                .appendQueryParameter("z", getQueryID())
                .build();
    }
}
