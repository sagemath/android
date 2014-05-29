package org.sagemath.droid.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Haven on 29-05-2014.
 */
public class PythonOutputData {

    @SerializedName("text/plain")
    private int outputValue;

    public int getOutputValue() {
        return outputValue;
    }
}
