package org.sagemath.droid.views;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import org.sagemath.droid.events.CodeReceivedEvent;
import org.sagemath.droid.utils.BusProvider;

/**
 * The JavaScript Interface to communicate with CodeMirror.
 *
 * @author Nikhil Peter Raj
 */
public class SageJavascriptInterface {
    private static String TAG = "SageDroid:JavaScriptInterface";

    private boolean forRun = false;
    private Activity activity;

    public SageJavascriptInterface(Activity activity) {
        this.activity = activity;
    }

    public boolean isForRun() {
        return forRun;
    }

    public void setForRun(boolean forRun) {
        this.forRun = forRun;
    }

    @JavascriptInterface
    public void getHtml(String html) {
        Log.i(TAG, "Got Text from Editor: " + html);
        if (html != null) {
            final CodeReceivedEvent event = new CodeReceivedEvent(html);
            event.setForRun(forRun);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BusProvider.getInstance().post(event);
                }
            });

        }
    }
}
