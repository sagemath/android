package org.sagemath.droid.constants;

/**
 * Constants corresponding to the Message Type reutrned by the Sage Server.
 *
 * @author Nikhil Peter Raj
 */
public class MessageType {
    public static final int ERROR = 0x000100;
    public static final int PYOUT = 0x000101;
    public static final int PYIN = 0x000102;
    public static final int STATUS = 0x000103;
    public static final int DISPLAY_DATA = 0x000104;
    public static final int STREAM = 0x000105;
    public static final int PYERR = 0x000106;
    public static final int EXECUTE_REQUEST = 0x000107;
    public static final int EXECUTE_REPLY = 0x000108;
    public static final int HTML_FILES = 0x000109;
    public static final int INTERACT_PREPARE = 0x000110;
    public static final int EXTENSION = 0x000111;
    public static final int TEXT_FILENAME = 0x000112;
    public static final int IMAGE_FILENAME = 0x000113;
    public static final int INTERACT = 0x000114;
    public static final int SAGE_CLEAR = 0x000115;
}
