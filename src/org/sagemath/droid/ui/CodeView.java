package org.sagemath.droid.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

/**
 * Created by Haven on 08-07-2014.
 */
public class CodeView extends WebView implements SageJavascriptInterface.OnHtmlReceivedListener {
    private static final String TAG = "Test:CodeView";

    private static final String INTERFACE_NAME = "JavaScriptInterface";

    private SageJavascriptInterface javaScriptInterface;
    private String receivedHtml;

    @SuppressWarnings("unused")
    public CodeView(Context context) {
        super(context);
        init();
    }

    @SuppressWarnings("unused")
    public CodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        javaScriptInterface = new SageJavascriptInterface();
        javaScriptInterface.setOnHtmlReceivedListener(this);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setBuiltInZoomControls(false);
        this.getSettings().setDefaultFixedFontSize(16);
        this.getSettings().setUseWideViewPort(true);
        this.getSettings().setLoadWithOverviewMode(true);
        this.addJavascriptInterface(javaScriptInterface, INTERFACE_NAME);
        loadUrl("file:///android_asset/codetest.html");
    }

    //Hackfix
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new CodeViewInputConnection(this, false);
    }

    @Override
    public void OnHtmlReceived(String html) {
        receivedHtml = html;
    }

    public String getEditorText() {
        this.post(new Runnable() {
            @Override
            public void run() {
                loadUrl("javascript:getText()");
            }
        });
        return receivedHtml;
    }

    public void setEditorText(String text) {
        final String functionCall = "javascript:setText(\"%s\")";
        final String textToSet = text;
        Log.i(TAG, "Calling js: " + String.format(functionCall, textToSet));
        this.post(new Runnable() {
            @Override
            public void run() {
                loadUrl(String.format(functionCall, textToSet));
            }
        });
    }

    private class CodeViewInputConnection extends BaseInputConnection {

        public CodeViewInputConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }

        // Hack to fix backspace.
        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 && afterLength == 0) {
                // backspace
                return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

}