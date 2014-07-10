package org.sagemath.droid.events;

/**
 * Created by Haven on 09-07-2014.
 */
public class ServerDisconnectEvent {
    private boolean isDisonnected;

    public ServerDisconnectEvent(boolean isDisonnected) {
        this.isDisonnected = isDisonnected;
    }

    public boolean isDisonnected() {
        return isDisonnected;
    }
}
