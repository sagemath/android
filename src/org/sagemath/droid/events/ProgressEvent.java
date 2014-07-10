package org.sagemath.droid.events;

/**
 * Created by Haven on 08-07-2014.
 */
public class ProgressEvent {
    private String progressState;

    public ProgressEvent(String progressState) {
        this.progressState = progressState;
    }

    public String getProgressState() {
        return progressState;
    }
}
