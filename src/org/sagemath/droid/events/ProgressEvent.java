package org.sagemath.droid.events;

/**
 * <p>The event which notifies whether progress has been started/stopped</p>
 *
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
