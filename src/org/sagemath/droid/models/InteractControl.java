package org.sagemath.droid.models;

import com.google.gson.annotations.SerializedName;

public class InteractControl {
    private boolean update;
    private boolean raw;
    private String control_type;
    private boolean display_value;
    private String[] values;

    @SerializedName("default")
    private int _default;

    private int[] range;
    private String subtype;
    private String label;
    private int step;

}
