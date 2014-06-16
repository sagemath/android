package org.sagemath.droid.models;

/**
 * @author Haven
 */
public class PythonErrorReply extends BaseReply {

    private PythonErrorContent content;

    public PythonErrorReply(){
        super();
    }

    public String toString() {
        return gson.toJson(this);
    }

    public static class PythonErrorContent {
        private String ename;
        private String evalue;

        public String getEname() {
            return ename;
        }

        public void setEname(String ename) {
            this.ename = ename;
        }

        public String getEvalue() {
            return evalue;
        }

        public void setEvalue(String evalue) {
            this.evalue = evalue;
        }
    }

    public PythonErrorContent getContent() {
        return content;
    }
}
