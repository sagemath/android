package org.sagemath.droid.events;

/**
 * <p>The event received when the websockets disconnect</p>
 *
 * @author Nikhil Peter Raj
 */
public class ServerDisconnectEvent {

    public static enum DisconnectType {
        DISCONNECT_TIMEOUT,
        DISCONNECT_INTERACT,
        DISCONNECT_HTTP_ERROR
    }

    private DisconnectType disconnectType;

    private boolean isInteractDisconnect;

    public ServerDisconnectEvent(DisconnectType disconnectType) {
        this.disconnectType = disconnectType;
    }

    public boolean isInteractDisconnect() {
        return isInteractDisconnect;
    }

    public DisconnectType getDisconnectType() {
        return disconnectType;
    }
}
