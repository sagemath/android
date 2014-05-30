package org.sagemath.droid.models;

import com.google.gson.annotations.SerializedName;

/**
 * @author Haven
 */
public class InteractControl {

    //The variable name that this control is associated with
    //Not part of the JSON that is received or replied
    //So we mark it transient to prevent GSON from deserialising/serialising it
    private transient String varName;

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

    public boolean isUpdate() {
        return update;
    }

    public boolean isRaw() {
        return raw;
    }

    public String getControlType() {
        return control_type;
    }

    public boolean isDisplayValue() {
        return display_value;
    }

    public String[] getValues() {
        return values;
    }

    public int getDefault() {
        return _default;
    }

    public int[] getRange() {
        return range;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getLabel() {
        return label;
    }

    public int getStep() {
        return step;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public void setRaw(boolean raw) {
        this.raw = raw;
    }

    public void setControlType(String control_type) {
        this.control_type = control_type;
    }

    public void setDisplayValue(boolean display_value) {
        this.display_value = display_value;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public void setDefault(int _default) {
        this._default = _default;
    }

    public void setRange(int[] range) {
        this.range = range;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }
}
