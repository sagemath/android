package org.sagemath.droid.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Haven on 28-05-2014.
 */
public class  InteractData {
    //---POSSIBLE SAGE REPLIES---
    @SerializedName("application/sage-interact")
    private SageInteract sageInteract;

    @SerializedName("text/plain")
    private String descText;

    public SageInteract getInteract() {
        return sageInteract;
    }

    public void setInteract(SageInteract sageInteract) {
        this.sageInteract = sageInteract;
    }

    public String getDescText() {
        return descText;
    }

    public void setDescText(String descText) {
        this.descText = descText;
    }
}
