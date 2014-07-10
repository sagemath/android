package org.sagemath.droid.events;

/**
 * Created by Haven on 09-07-2014.
 */
public class InteractDisconnectEvent {
    private boolean isDisonnected;

    public InteractDisconnectEvent(boolean isDisonnected) {
        this.isDisonnected = isDisonnected;
    }

    public boolean isDisonnected() {
        return isDisonnected;
    }
}
