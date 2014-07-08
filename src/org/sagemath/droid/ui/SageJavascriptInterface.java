package org.sagemath.droid.ui;

import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created by Haven on 08-07-2014.
 */
public class SageJavascriptInterface {
    private static String TAG = "SageDroid:JavaScriptInterface";

    public interface OnHtmlReceivedListener {
        public void OnHtmlReceived(String html);
    }

    private OnHtmlReceivedListener listener;

    public void setOnHtmlReceivedListener(OnHtmlReceivedListener listener) {
        this.listener = listener;
    }

    @JavascriptInterface
    public void getHtml(String html) {
        Log.i(TAG, "Got Text from Editor: " + html);
        listener.OnHtmlReceived(html);
    }
}
