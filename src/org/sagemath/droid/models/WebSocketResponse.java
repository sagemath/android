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

    public boolean isValidResponse() {
        //Some ugly sanity checking, for flavor
        if ((kernel_id != null || kernel_id != "") && (ws_url != null || ws_url != "")) {
            return true;
        }
        return false;
    }

}
