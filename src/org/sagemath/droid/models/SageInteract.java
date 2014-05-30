package org.sagemath.droid.models;

import java.util.ArrayList;

public class SageInteract {

    private String new_interact_id;
    private InteractControl controls;
    private boolean readonly;
    private String locations;
    private ArrayList<ArrayList<ArrayList<String>>> layout;

    public String getNewInteractID() {
        return new_interact_id;
    }

    public void setNewInteractID(String new_interact_id) {
        this.new_interact_id = new_interact_id;
    }

    public InteractControl getControls() {
        return controls;
    }

    public void setControls(InteractControl controls) {
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
