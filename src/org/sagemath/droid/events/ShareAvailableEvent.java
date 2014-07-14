package org.sagemath.droid.events;

/**
 * Created by Haven on 13-07-2014.
 */
public class ShareAvailableEvent {
    private String shareURL;

    public ShareAvailableEvent(String shareURL) {
        this.shareURL = shareURL;
    }

    public String getShareURL() {
        return shareURL;
    }
}
