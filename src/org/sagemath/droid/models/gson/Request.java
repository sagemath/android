package org.sagemath.droid.models.gson;

import java.util.ArrayList;

public class Request {
    private static final String EXECUTE_REQUEST="execute_request";
    //private static final String

    private Header header;
    private Header parent_header;
    private RequestContent content;
    private MetaData metadata;

    public Request() {
        header = new Header();
        parent_header= new Header();
        content = new RequestContent();
        metadata = new MetaData();
    }

    public Request(String sageInput) {
        this();
        getHeader().init(); // With random UUID
        getHeader().setMessageType(EXECUTE_REQUEST);
        getContent().setCode(sageInput);
    }

    public Request(String sageInput, String session) {
        this();
        getHeader().init(session);
        getHeader().setMessageType(EXECUTE_REQUEST);
        getContent().setCode(sageInput);

    }

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

        public RequestContent() {
            user_variables = new ArrayList<String>();
            user_expressions = new UserExpressions();
            setSilent(false);
            setAllowStdin(false);
        }

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
