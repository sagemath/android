package org.sagemath.droid.events;

/**
 * @author Nikhil Peter Raj
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
