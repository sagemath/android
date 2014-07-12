package org.sagemath.droid.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.sagemath.droid.constants.StringConstants;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.events.ProgressEvent;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.models.gson.*;
import org.sagemath.droid.utils.BusProvider;

import java.util.ArrayList;

public class OutputWebView extends WebView {
    private final static String TAG = "SageDroid:OutputBlock";

    private Context context;
    private ArrayList<String> divs = new ArrayList<String>();
    private boolean isImageDiv = false;
    private boolean isHtmlScriptDiv = false;
    private String htmlData;
    private Cell cell;
    private SageSQLiteOpenHelper helper;

    public OutputWebView(Context context, Cell cell) {
        super(context);
        this.context = context;
        this.cell = cell;
        init();

    }

    public OutputWebView(Context context, Cell cell, String htmlData) {
        super(context);
        this.htmlData = htmlData;
        this.context = context;
        this.cell = cell;
        init();
        Log.i(TAG, "Created outputblock from htmldata.");
        divs.clear();
        divs.add(htmlData);
        try {
            Log.i(TAG, "outputblock created: " + " " + cell.getTitle() + " " + cell.getUUID().toString() + " " + " ");
        } catch (Exception e) {
            Log.i(TAG, "outputblock exception: " + e.getMessage());
        }
    }

    private void init() {
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setBuiltInZoomControls(true);
        this.setWebViewClient(client);
        helper = SageSQLiteOpenHelper.getInstance(context);
        BusProvider.getInstance().register(this);
    }

    public void unregister() {
        BusProvider.getInstance().unregister(this);
    }

    private static String htmlify(String str) {
        Log.i(TAG, "Converting to HTML: " + str);
        StringBuilder s = new StringBuilder();
        s.append("<pre style=\"font-size:130%\">");
        String[] lines = str.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (i > 0)
                s.append("&#13;&#10;");
            s.append(TextUtils.htmlEncode(lines[i]));
        }
        s.append("</pre>");
        Log.i(TAG, "Returning converted HTML: " + s.toString());
        return s.toString();
    }

    private String getHtml() {
        StringBuilder s = new StringBuilder();
        s.append("<html>");
        //Configure & Load MathJax
        if (isHtmlScriptDiv) {
            s.append(StringConstants.MATHJAX_CONFIG);
            s.append(StringConstants.MATHJAX_CDN);
        }
        if (isImageDiv) {
            s.append(StringConstants.IMAGE_STYLE);
        }
        s.append("<body>");
        Log.i(TAG, "Constructing HTML with: " + divs.size() + "divs");
        for (String div : divs) {
            s.append("<div>");
            Log.i(TAG, "Adding div" + div);
            s.append(div);
            s.append("</div>");
        }
        s.append("</body>");
        s.append("</html>");
        return s.toString();
    }

    private void addDiv(BaseReply reply) {
        //Log.i(TAG, "Adding Reply: " + reply.toString());

        if (reply instanceof ImageReply) {
            Log.i(TAG, "Adding an ImageReply");
            addDivImageReply((ImageReply) reply);
        } else if (reply instanceof HtmlReply) {
            //Having text/html
            Log.i(TAG, "Adding HTML Reply");
            addDivHtmlReply((HtmlReply) reply);
        } else if (reply instanceof PythonOutputReply) {
            //Having pyout
            Log.i(TAG, "Adding PyOut Reply");
            addDivPythonOutputReply((PythonOutputReply) reply);
        } else if (reply instanceof PythonErrorReply) {
            //Having pyerr, Traceback
            Log.i(TAG, "Adding PyErr Reply");
            addDivPythonErrorReply((PythonErrorReply) reply);
        } else if (reply instanceof StreamReply) {
            //Having Stream
            Log.i(TAG, "Adding Stream Reply");
            addDivStreamReply((StreamReply) reply);
        } else if (reply instanceof StatusReply) {
            //Only Idle or Dead Status Reply is possible here, hence simply load URL
            Log.i(TAG, "Got Idle Status, Loading URL");
            loadSavedUrl();
        } else {
            Log.i(TAG, "Unknown Output");
        }
    }

    /**
     * Add an image to the div
     *
     * @param reply
     */
    private void addDivImageReply(ImageReply reply) {
        isImageDiv = true;
        String jpgDivTemplate = "<img src=\"%s\"alt=\"plot output\"></img>";
        String svgDivTemplate = "<object data=\"%s\" type=\"image/svg+xml\"></img>";
        String jpgDiv, svgDiv;

        if (reply.getImageMimeType().equals(ImageReply.MIME_IMAGE_PNG)) {
            Log.i(TAG, "Image is in .png format");
            jpgDiv = String.format(jpgDivTemplate, reply.getImageURL());
            Log.i(TAG, "Adding .png div" + jpgDiv);
            divs.add(jpgDiv);
        } else if (reply.getImageMimeType().equals(ImageReply.MIME_IMAGE_SVG)) {
            Log.i(TAG, "Image is in .svg format");
            svgDiv = String.format(svgDivTemplate, reply.getImageURL());
            Log.i(TAG, "Adding .svg div" + svgDiv);
            divs.add(svgDiv);
        } else if (reply.getImageMimeType() == null) {
            Log.i(TAG, "Unknown Image Type");
            String div = "Unknown MIME type";
            divs.add(div);
        }
    }

    private void addDivHtmlReply(HtmlReply reply) {
        String html = reply.getContent().getData().getHtmlCode();
        if (html.contains("script")) {
            isHtmlScriptDiv = true;
        }
        divs.add(html);
    }

    private void addDivPythonOutputReply(PythonOutputReply reply) {
        String outputValue = reply.getContent().getData().getOutputValue();
        //If the outputValue is empty, don't add it, might overwrite data which is actually valid.
        if (!outputValue.equalsIgnoreCase("")) {
            String div = htmlify(outputValue);
            divs.add(div);
        }
    }

    private void addDivStreamReply(StreamReply reply) {
        String div = htmlify(reply.getContent().getData());
        divs.add(div);
    }

    private void addDivPythonErrorReply(PythonErrorReply reply) {
        String div = htmlify(reply.getContent().getEname() + ":" + reply.getContent().getEvalue());
        divs.add(div);

    }

    public void add(BaseReply reply) {
        if (reply instanceof SageClearReply) {
        }
        addDiv(reply);
    }

    public void loadSavedUrl() {
        htmlData = getHtml();
        Log.i(TAG, "Loading html: " + htmlData);
        this.post(new Runnable() {
            @Override
            public void run() {
                loadData(htmlData, "text/html", "utf-8");
            }
        });

    }


    public void reloadHtml(String savedHtml) {
        Log.i(TAG, "Loading Saved HTML" + htmlData);
        loadData(htmlData, "text/html", "utf-8");
        reload();
    }

    public void set(BaseReply reply) {
        Log.i(TAG, "Clearing divs");
        add(reply);
    }

    public String getHtmlData() {
        if (htmlData != null)
            return htmlData;
        return null;
    }

    public void setHtmlFromSavedState(String html) {
        Log.i(TAG, "Setting HTML from saved state" + html);
        this.htmlData = html;
        loadData(htmlData, "text/html", "utf-8");
    }

    public void clearBlocks() {
        divs.clear();
    }

    private WebViewClient client = new WebViewClient() {


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            BusProvider.getInstance().post(new ProgressEvent(StringConstants.ARG_PROGRESS_START));

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            BusProvider.getInstance().post(new ProgressEvent(StringConstants.ARG_PROGRESS_END));
        }
    };

}
