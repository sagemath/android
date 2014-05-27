package org.sagemath.droid.models;

/**
 * Created by Haven on 27-05-2014.
 */
public class WebSocketResponse {

    private String kernel_id;
    private String ws_url;

    public String getKernelID() {
        return kernel_id;
    }

    public String getWebSocketURL() {
        return ws_url;
    }
}
