package org.sagemath.droid;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import org.sagemath.droid.cells.CellData;
import org.sagemath.droid.models.*;

import java.util.LinkedList;
import java.util.ListIterator;

public class OutputBlock extends WebView {
    private final static String TAG = "SageDroid:OutputBlock";

    private final CellData cell;
    private LinkedList<String> divs = new LinkedList<String>();

    public OutputBlock(Context context, CellData cell) {
        super(context);
        this.cell = cell;
    }

    public OutputBlock(Context context, CellData cell, String htmlData) {
        super(context);
        Log.i(TAG, "Created outputblock from htmldata.");
        this.cell = cell;
        divs.clear();
        divs.add(htmlData);
        try {
            Log.i(TAG, "outputblock created: " + " " + cell.getTitle() + " " + cell.getUUID().toString() + " " + " ");
        } catch (Exception e) {
            Log.i(TAG, "outputblock exception: " + e.getMessage());
        }
    }

    // The output_block field of the JSON message
    protected String name;

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
        return s.toString();
    }

    public String getHtml() {
        StringBuilder s = new StringBuilder();
        s.append("<html><body>");
        ListIterator<String> iter = divs.listIterator();
        while (iter.hasNext()) {
            s.append("<div>");
            s.append(iter.next());
            s.append("</div>");
        }
        s.append("</body></html>");
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
            addDivPythonOutput((PythonOutputReply) reply);
        } else if (reply instanceof PythonErrorReply) {
            //Having pyerr, Traceback
            Log.i(TAG, "Adding PyErr Reply");
            addDivPythonErrorReply((PythonErrorReply) reply);
        } else if (reply instanceof StreamReply) {
            //Having Stream
            Log.i(TAG, "Adding Stream Reply");
            addDivStreamReply((StreamReply) reply);
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
        String url = reply.getImageURL();
        String jpgDivTemplate = "<img src=\"%s\"alt=\"plot output\"></img>";
        String svgDivTemplate = "<object data=\"%s\" type=\"image/svg+xml\"></img>";
        String jpgDiv, svgDiv;

        if (reply.getImageMimeType().equals(ImageReply.MIME_IMAGE_PNG)) {
            Log.i(TAG, "Image is in .png format");
            jpgDiv = String.format(jpgDivTemplate, reply.getImageURL());
            Log.i(TAG, "Adding .png div" + jpgDiv);
            divs.add(jpgDiv);
            loadSavedUrl();
        } else if (reply.getImageMimeType().equals(ImageReply.MIME_IMAGE_SVG)) {
            Log.i(TAG, "Image is in .svg format");
            svgDiv = String.format(svgDivTemplate, reply.getImageURL());
            Log.i(TAG, "Adding .svg div" + svgDiv);
            divs.add(svgDiv);
            loadSavedUrl();
        } else if (reply.getImageMimeType() == null) {
            Log.i(TAG, "Unknown Image Type");
            String div = "Unknown MIME type";
            divs.add(div);
            loadSavedUrl();
        }
    }

    private void addDivHtmlReply(HtmlReply reply) {
        divs.add(reply.getContent().getData().getHtmlCode());
    }

    private void addDivPythonOutput(PythonOutputReply reply) {
        String outputValue = reply.getContent().getData().getOutputValue();
        //If the outputValue is empty, don't add it, might overwrite data which is actually valid.
        if (!outputValue.equalsIgnoreCase("")) {
            String div = htmlify(outputValue);
            divs.add(div);
            loadSavedUrl();
        }
    }

    private void addDivStreamReply(StreamReply reply) {
        String div = htmlify(reply.getContent().getData());
        divs.add(div);
        loadSavedUrl();
    }

    private void addDivPythonErrorReply(PythonErrorReply reply) {
        String div = htmlify(reply.getContent().getEname() + ":" + reply.getContent().getEvalue());
        divs.add(div);
        loadSavedUrl();
    }

    public void add(BaseReply reply) {

        if (reply instanceof SageClearReply) {
            Log.i(TAG, "Sage Clear Reply");
            //divs.clear();
        }
        addDiv(reply);
    }

    public void loadSavedUrl() {
        cell.saveOutput("", getHtml());
        loadUrl(cell.getUrlString(""));
    }

    public void set(String output_block) {
        Log.i(TAG, "set(String output_block");
        if (cell.hasCachedOutput(output_block))
            loadUrl(cell.getUrlString(output_block));
    }

    public void set(BaseReply reply) {
        divs.clear();
        add(reply);
    }

    public void clearBlocks() {
        divs.clear();
    }

    public void setHistoryHTML() {
        loadUrl(cell.getUrlString(cell.getUUID().toString()));
    }

    public String getHTML() {
        String htmldata = "";
        for (String div : divs) {
            htmldata += div;
        }
        return htmldata;
    }
}
