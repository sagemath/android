package org.sagemath.droid.events;

/**
 * <p>The event which notifies availability of share url etc</p>
 *
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
