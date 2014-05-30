package org.sagemath.droid.models;

import com.google.gson.annotations.Expose;

public class InteractContent {

    @Expose
    private InteractData data;
    private String source;

    public InteractData getData() {
        return data;
    }

    public void setData(InteractData data) {
        this.data = data;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
