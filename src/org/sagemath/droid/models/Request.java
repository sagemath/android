package org.sagemath.droid.models;

import java.util.ArrayList;

public class Request {

    private Header header;
    private Header parent_header;
    private RequestContent content;
    private MetaData metadata;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Header getParentHeader() {
        return parent_header;
    }

    public void setParentHeader(Header parent_header) {
        this.parent_header = parent_header;
    }

    public RequestContent getContent() {
        return content;
    }

    public void setContent(RequestContent content) {
        this.content = content;
    }

    public MetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(MetaData metadata) {
        this.metadata = metadata;
    }

    public static class RequestContent {

        private String code;
        private boolean silent;
        private ArrayList<String> user_variables;
        private UserExpressions user_expressions;
        private boolean allow_stdin;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public boolean isSilent() {
            return silent;
        }

        public void setSilent(boolean silent) {
            this.silent = silent;
        }

        public ArrayList<String> getUserVariables() {
            return user_variables;
        }

        public void setUserVariables(ArrayList<String> user_variables) {
            this.user_variables = user_variables;
        }

        public boolean isAllowStdin() {
            return allow_stdin;
        }

        public void setAllowStdin(boolean allow_stdin) {
            this.allow_stdin = allow_stdin;
        }

        public static class UserExpressions {

            private String _sagecell_files = "sys._sage_.new_files()";
        }


    }

    public static class MetaData {

    }
}
