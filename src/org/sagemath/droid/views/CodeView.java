package org.sagemath.droid.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Created by Haven on 08-07-2014.
 */
public class CodeView extends WebView {
    private static final String TAG = "Test:CodeView";

    private static final String INTERFACE_NAME = "JavaScriptInterface";

    private SageJavascriptInterface javaScriptInterface;

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
        javaScriptInterface = new SageJavascriptInterface((Activity) getContext());
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setBuiltInZoomControls(false);
        this.getSettings().setDefaultFixedFontSize(16);
        this.getSettings().setUseWideViewPort(true);
        this.getSettings().setLoadWithOverviewMode(true);
        this.addJavascriptInterface(javaScriptInterface, INTERFACE_NAME);
        this.post(new Runnable() {
            @Override
            public void run() {
                loadUrl("file:///android_asset/codetest.html");
            }
        });

    }

    //Hackfix
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new CodeViewInputConnection(this, false);
    }

    public void getEditorText(boolean forRun) {
        javaScriptInterface.setForRun(forRun);
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadUrl("javascript:getEditorText();");
            }
        },500);
    }

    public void setEditorText(String text) {
        final String functionCall = "javascript:setEditorText(\"%s\");";
        final String textToSet = StringEscapeUtils.escapeJavaScript(text);
        Log.i(TAG, "Calling js: " + String.format(functionCall, textToSet));
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadUrl(String.format(functionCall, textToSet));
            }
        },500);
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