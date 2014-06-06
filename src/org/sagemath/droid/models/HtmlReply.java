package org.sagemath.droid.models;

import com.google.gson.annotations.SerializedName;

/**
 * @author Haven
 */
public class HtmlReply extends BaseReply {

    private HtmlContent content;

    public static class HtmlContent {

        private HtmlData data;

        public HtmlData getData() {
            return data;
        }
    }

    public static class HtmlData {

        @SerializedName("text/html")
        private String htmlCode;

        @SerializedName("text/plain")
        private String descText;

        public String getHtmlCode() {
            return htmlCode;
        }

        public void setHtmlCode(String htmlCode) {
            this.htmlCode = htmlCode;
        }

        public String getDescText() {
            return descText;
        }

        public void setDescText(String descText) {
            this.descText = descText;
        }
    }

    public HtmlContent getContent() {
        return content;
    }
}