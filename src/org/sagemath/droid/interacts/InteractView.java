package org.sagemath.droid.interacts;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import org.sagemath.droid.constants.ControlType;
import org.sagemath.droid.events.InteractUpdateEvent;
import org.sagemath.droid.models.gson.InteractReply;
import org.sagemath.droid.models.gson.InteractReply.InteractControl;
import org.sagemath.droid.models.gson.InteractReply.SageInteract;
import org.sagemath.droid.utils.BusProvider;

import java.util.ArrayList;

public class InteractView extends TableLayout {
    private final static String TAG = "SageDroid:InteractView";

    private static final String ARG_INTERACTS = "interacts";

    private Context context;
    private ArrayList<View> addedViews;

    public InteractView(Context context) {
        super(context);
        this.context = context;
        BusProvider.getInstance().register(this);
        addedViews = new ArrayList<View>();
        setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
    }

    public void unregister() {
        BusProvider.getInstance().unregister(this);
    }

    private InteractReply interactReply;

    public void set(InteractReply interactReply) {
        Log.i(TAG, "Setting InteractReply" + interactReply.toString());
        this.interactReply = interactReply;
        removeAllViews();

        SageInteract sageInteract = interactReply.getContent().getData().getInteract();

        //For each control present, add the corresponding type.
        for (InteractControl control : sageInteract.getControls()) {
            addInteract(control, false);
        }
    }

    /**
     * Add an Interact Control to the output
     *
     * @param control        The {@link org.sagemath.droid.models.gson.InteractReply.InteractControl} for this view
     * @param fromSavedState Whether to restore from saved state or not
     */
    public void addInteract(InteractControl control, boolean fromSavedState) {
        Log.i(TAG, "Processing InteractControl" + control.toString());
        switch (control.getControlType()) {
            case ControlType.CONTROL_SLIDER:
                if (control.getSubtype() == ControlType.SLIDER_CONTINUOUS) {
                    if (!fromSavedState)
                        addContinuousSlider(control);
                    else
                        addContinuousSlider(control, control.getIntSavedValue(), control.isViewEnabled());
                } else if (control.getSubtype() == ControlType.SLIDER_DISCRETE) {
                    //Add discrete slider
                    if (!fromSavedState)
                        addDiscreteSlider(control);
                    else
                        addDiscreteSlider(control, control.getIntSavedValue(), control.isViewEnabled());
                }
                break;

            case ControlType.CONTROL_SELECTOR:
                //Add a selector
                if (!fromSavedState)
                    addSelector(control);
                else
                    addSelector(control, control.getStringSavedValue(), control.isViewEnabled());
                break;
        }
    }

    public void addInteractsFromSavedState(ArrayList<InteractControl> savedControls) {
        addedViews.clear();
        for (InteractControl control : savedControls) {
            addInteract(control, true);
        }
    }

    protected void addContinuousSlider(InteractControl control) {
        Log.i(TAG, "Adding Continous Slider");
        InteractContinuousSlider slider = new InteractContinuousSlider(this, control.getVarName(), context);
        slider.setRange(control);
        addView(slider);
        addedViews.add(slider);
    }

    public void addContinuousSlider(InteractControl control, int savedValue, boolean enabled) {
        Log.i(TAG, "Add Slider from Saved State with value: " + savedValue + "enabled: " + enabled);
        InteractContinuousSlider slider = new InteractContinuousSlider(this, control.getVarName(), context);
        slider.setRange(control);
        slider.getSeekBar().setProgress(savedValue);
        slider.getSeekBar().setEnabled(enabled);
        addView(slider);
        addedViews.add(slider);
    }

    protected void addDiscreteSlider(InteractControl control) {
        Log.i(TAG, "Adding Discrete Slider");
        InteractDiscreteSlider slider = new InteractDiscreteSlider(this, control.getVarName(), context);
        slider.setValues(control);
        addView(slider);
        addedViews.add(slider);
    }

    public void addDiscreteSlider(InteractControl control, int savedValue, boolean enabled) {
        Log.i(TAG, "Add Slider from Saved State with value: " + savedValue + "enabled: " + enabled);
        InteractDiscreteSlider slider = new InteractDiscreteSlider(this, control.getVarName(), context);
        slider.setValues(control);
        slider.getSeekBar().setProgress(savedValue);
        slider.getSeekBar().setEnabled(enabled);
        addView(slider);
        addedViews.add(slider);
    }

    protected void addSelector(InteractControl control) {
        Log.i(TAG, "Adding a selector");
        InteractSelector selector = new InteractSelector(this, control.getVarName(), context);
        selector.setValues(control);
        addView(selector);
        addedViews.add(selector);
    }

    public void addSelector(InteractControl control, String savedSelection, boolean enabled) {
        Log.i(TAG, "Adding a selector");
        InteractSelector selector = new InteractSelector(this, control.getVarName(), context);
        selector.setValues(control);
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) selector.getSpinner().getAdapter();
        int pos = adapter.getPosition(savedSelection);
        selector.getSpinner().setSelection((pos == -1) ? 0 : pos);
        selector.getSpinner().setEnabled(enabled);
        addView(selector);
        addedViews.add(selector);
    }

    protected void notifyChange(InteractControlBase view) {
        //listener.onInteractListener(interactReply, view.getVariableName(), view.getValue());
        BusProvider.getInstance().post(new InteractUpdateEvent(interactReply, view.getVariableName(), view.getValue()));
    }

    public ArrayList<View> getAddedViews() {
        return addedViews;
    }

    public void disableViews() {
        Log.i(TAG, "Disabling Views");
        for (View v : addedViews) {
            if (v instanceof InteractContinuousSlider) {
                ((InteractContinuousSlider) v).getSeekBar().setEnabled(false);
            } else if (v instanceof InteractDiscreteSlider) {
                ((InteractDiscreteSlider) v).getSeekBar().setEnabled(false);
            } else if (v instanceof InteractSelector) {
                ((InteractSelector) v).getSpinner().setEnabled(false);
            }
        }
    }

    public void enableViews() {
        Log.i(TAG, "Enabling Views");
        for (View v : addedViews) {
            if (v instanceof InteractContinuousSlider) {
                ((InteractContinuousSlider) v).getSeekBar().setEnabled(true);
            } else if (v instanceof InteractDiscreteSlider) {
                ((InteractDiscreteSlider) v).getSeekBar().setEnabled(true);
            } else if (v instanceof InteractSelector) {
                ((InteractSelector) v).getSpinner().setEnabled(true);
            }
        }
    }

}
