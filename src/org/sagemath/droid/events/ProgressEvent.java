package org.sagemath.droid.events;

/**
 * @author Nikhil Peter Raj
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
