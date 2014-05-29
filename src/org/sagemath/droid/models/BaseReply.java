package org.sagemath.droid.models;

/**
 * Base Reply from the Server
 */
public class BaseReply {

    protected Header header;
    protected Header parent_header;
    protected MetaData metadata;
    protected String msg_type;
    protected String msg_id;

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

    public MetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(MetaData metadata) {
        this.metadata = metadata;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getMessageID() {
        return msg_id;
    }

    public void setMessageID(String msg_id) {
        this.msg_id = msg_id;
    }
}
