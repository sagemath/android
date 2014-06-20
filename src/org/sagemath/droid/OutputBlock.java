package org.sagemath.droid;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import org.apache.commons.io.IOUtil;
import org.sagemath.droid.cells.CellData;
import org.sagemath.droid.constants.StringConstants;
import org.sagemath.droid.models.*;

import java.util.ArrayList;

public class OutputBlock extends WebView {
    private final static String TAG = "SageDroid:OutputBlock";

    private Context context;
    private final CellData cell;
    private ArrayList<String> divs = new ArrayList<String>();

    public OutputBlock(Context context, CellData cell) {
        super(context);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setBuiltInZoomControls(true);
        this.context = context;
        this.cell = cell;
    }

    public OutputBlock(Context context, CellData cell, String htmlData) {
        super(context);
        this.context = context;
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setBuiltInZoomControls(true);
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
        Log.i(TAG, "Returning converted HTML: " + s.toString());
        return s.toString();
    }

    public String getHtml() {
        StringBuilder s = new StringBuilder();
        s.append("<html>");
        //Configure & Load MathJax
        s.append(StringConstants.MATHJAX_CONFIG);
        s.append(StringConstants.MATHJAX_CDN);
        s.append(StringConstants.IMAGE_STYLE);
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
        cell.saveOutput("test", getHtml());
        Log.i(TAG, "Loading URL:" + cell.getUrlString("test"));
        try {
            Log.i(TAG, "Loading HTML:" + IOUtil.toString(context.getResources().getAssets().open(cell.getUrlString("test"))));
        } catch (Exception e) {
            Log.i(TAG, e + "");
        }
        Log.i(TAG, "Loading HTML:" + getHtml());
        loadData(getHtml(), "text/html", "utf-8");
    }

    public void set(String output_block) {
        Log.i(TAG, "set(String output_block");
        if (cell.hasCachedOutput(output_block))
            loadUrl(cell.getUrlString(output_block));
    }

    public void set(BaseReply reply) {
        Log.i(TAG, "Clearing divs");
        //divs.clear();
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

    public String doubleEscapedLatex(String input) {
        String escapedString = "";
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '\'') escapedString += '\\';
            if (input.charAt(i) != '\n') escapedString += input.charAt(i);
            if (input.charAt(i) == '\\') escapedString += "\\";
        }
        return escapedString;
    }
}
