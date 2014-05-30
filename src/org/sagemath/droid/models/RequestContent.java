package org.sagemath.droid.models;

import java.util.ArrayList;

public class RequestContent {

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
}
