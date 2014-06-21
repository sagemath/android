package org.sagemath.droid.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.sagemath.droid.constants.ControlType;

import java.util.ArrayList;

/**
 * @author Haven
 */
public class InteractReply extends BaseReply {

    private InteractContent content;

    public InteractReply() {
        super();
    }

    public String toString() {
        return gson.toJson(this);
    }

    public InteractContent getContent() {
        return content;
    }


    public static class InteractContent {

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

    public static class InteractData {
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

    public static class SageInteract {

        private String new_interact_id;
        private ArrayList<InteractControl> controls;
        private boolean readonly;
        private String locations;
        private ArrayList<ArrayList<ArrayList<String>>> layout;

        public String getNewInteractID() {
            return new_interact_id;
        }

        public void setNewInteractID(String new_interact_id) {
            this.new_interact_id = new_interact_id;
        }

        public ArrayList<InteractControl> getControls() {
            return controls;
        }

        public void setControls(ArrayList<InteractControl> controls) {
            this.controls = controls;
        }

        public boolean isReadonly() {
            return readonly;
        }

        public void setReadonly(boolean readonly) {
            this.readonly = readonly;
        }

        public String getLocations() {
            return locations;
        }

        public void setLocations(String locations) {
            this.locations = locations;
        }

        public ArrayList<ArrayList<ArrayList<String>>> getLayout() {
            return layout;
        }

        public void setLayout(ArrayList<ArrayList<ArrayList<String>>> layout) {
            this.layout = layout;
        }
    }

    public static class InteractControl {

        private static final String STR_SLIDER = "slider";
        private static final String STR_SELECTOR = "selector";
        private static final String STR_DISCRETE = "discrete";
        private static final String STR_CONTINUOUS = "continuous";

        //The variable name that this control is associated with
        //Not part of the JSON that is received or replied
        //So we mark it transient to prevent GSON from deserialising/serialising it
        private transient String varName;
        private transient Gson gson;

        private boolean update;
        private boolean raw;
        private String control_type;
        private boolean display_value;
        private String[] value_labels;
        private Values values;

        @SerializedName("default")
        private int _default;

        private int[] range;
        private String subtype;
        private String label;
        private int step;

        public InteractControl() {
            gson = new Gson();
        }

        public String toString() {
            //TODO, Try and use the gson from BaseReply
            return gson.toJson(this);
        }

        public boolean isUpdate() {
            return update;
        }

        public boolean isRaw() {
            return raw;
        }

        public String getStringControlType() {
            return control_type;
        }

        public int getControlType() {
            if (control_type.equalsIgnoreCase(STR_SELECTOR))
                return ControlType.CONTROL_SELECTOR;
            else if (control_type.equalsIgnoreCase(STR_SLIDER))
                return ControlType.CONTROL_SLIDER;
            else return ControlType.CONTROL_ERROR;
        }

        public boolean isDisplayValue() {
            return display_value;
        }

        public Values getValues() {
            return values;
        }

        public int getDefault() {
            return _default;
        }

        public int[] getRange() {
            return range;
        }

        public String getStringSubtype() {
            return subtype;
        }

        public int getSubtype() {
            if (subtype.equalsIgnoreCase(STR_DISCRETE))
                return ControlType.SLIDER_DISCRETE;
            else if (subtype.equalsIgnoreCase(STR_CONTINUOUS))
                return ControlType.SLIDER_CONTINUOUS;
            else return ControlType.CONTROL_ERROR;
        }

        public String getLabel() {
            return label;
        }

        public String[] getValueLabels() {
            return value_labels;
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

        public void setValue(Values values) {
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

}
