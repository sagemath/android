package org.sagemath.droid.events;

/**
 * <p>The event received when the websockets disconnect</p>
 *
 * @author Nikhil Peter Raj
 */
public class ServerDisconnectEvent {
    private boolean isDisconnected;

    public ServerDisconnectEvent(boolean isDisconnected) {
        this.isDisconnected = isDisconnected;
    }

    public boolean isDisconnected() {
        return isDisconnected;
    }
}
