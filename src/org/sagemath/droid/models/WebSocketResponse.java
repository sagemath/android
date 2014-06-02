package org.sagemath.droid.models;

/**
 * @author Haven
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
