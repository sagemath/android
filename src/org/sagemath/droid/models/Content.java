package org.sagemath.droid.models;

import java.util.ArrayList;

/**
 * Generic Content Model
 */
public class Content {

    private String code;
    private boolean allow_stdin;

    //--REQUEST--
    private boolean silent;
    private UserExpressions user_expressions;
    private ArrayList<String> user_variables;

    //---REPLY---
    private int execution_count;
    private String execution_state;

    //---INTERACT RELATED---
    private String msg_id;
    private String source;
    private Data data;


}
