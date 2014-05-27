package org.sagemath.droid.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Haven on 27-05-2014.
 */
public class Data {

    @SerializedName("application/sage-interact")
    private SageInteract sageInteract;
    @SerializedName("text/plain")
    private String dataType;


}
