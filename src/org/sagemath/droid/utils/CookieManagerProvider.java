package org.sagemath.droid.utils;

import java.net.CookieManager;
import java.net.CookiePolicy;

/**
 * @author Nikhil Peter Raj
 *
 * Singleton which returns a {@linkplain java.net.CookieManager}
 *
 */
public class CookieManagerProvider {
    private static final String TAG = "SageDroid:CookieManagerProvider";

    private static CookieManager cookieManager = null;

    private CookieManagerProvider() {

    }

    public static CookieManager getInstance() {
        if (cookieManager == null) {
            cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        }
        return cookieManager;
    }

}
