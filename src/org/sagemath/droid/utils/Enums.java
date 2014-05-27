package org.sagemath.droid.utils;

public class Enums {

    public static enum MSG_TYPE {
        ERROR, PYOUT, STATUS, PYIN, DISPLAY_DATA, STREAM, PYERR, EXEC_REPLY, EXTENSION, SESSION_END,
        HTML_FILES, INTERACT_PREPARE
    }

    public static enum DATA_TYPE {FILENAME, IMAGE_FILENAME, INTERACT}
    public static enum EXEC_STATE{BUSY,DEAD,IDLE}
}
