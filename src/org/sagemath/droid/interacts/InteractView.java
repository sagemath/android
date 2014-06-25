package org.sagemath.droid.interacts;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import org.sagemath.droid.constants.ControlType;
import org.sagemath.droid.models.gson.InteractReply;
import org.sagemath.droid.models.gson.InteractReply.InteractControl;
import org.sagemath.droid.models.gson.InteractReply.SageInteract;

import java.util.ArrayList;

public class InteractView extends TableLayout {
    private final static String TAG = "SageDroid:InteractView";

    private Context context;
    private ArrayList<View> addedViews;

    public InteractView(Context context) {
        super(context);
        this.context = context;
        addedViews = new ArrayList<View>();
        setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
    }

    public interface OnInteractListener {
        public void onInteractListener(InteractReply interact, String name, Object value);
    }

    private OnInteractListener listener;

    public void setOnInteractListener(OnInteractListener listener) {
        this.listener = listener;
    }

    private InteractReply interactReply;

    public void set(InteractReply interactReply) {
        Log.i(TAG, "Setting InteractReply" + interactReply.toString());
        this.interactReply = interactReply;
        removeAllViews();

        SageInteract sageInteract = interactReply.getContent().getData().getInteract();

        //For each control present, add the corresponding type.
        for (InteractControl control : sageInteract.getControls()) {
            addInteract(control);
        }
    }

    /**
     * Add an Interact Control to the output
     *
     * @param control
     */
    public void addInteract(InteractControl control) {
        Log.i(TAG, "Processing InteractControl" + control.toString());
        switch (control.getControlType()) {
            case ControlType.CONTROL_SLIDER:
                if (control.getSubtype() == ControlType.SLIDER_CONTINUOUS) {
                    addContinuousSlider(control);
                } else if (control.getSubtype() == ControlType.SLIDER_DISCRETE) {
                    //Add discrete slider
                    addDiscreteSlider(control);
                }
                break;

            case ControlType.CONTROL_SELECTOR:
                //Add a selector
                addSelector(control);
                break;
        }
    }

    protected void addContinuousSlider(InteractControl control) {
        Log.i(TAG, "Adding Continous Slider");
        InteractContinuousSlider slider = new InteractContinuousSlider(this, control.getVarName(), context);
        slider.setRange(control);
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

    protected void addSelector(InteractControl control) {
        Log.i(TAG, "Adding a selector");
        InteractSelector selector = new InteractSelector(this, control.getVarName(), context);
        selector.setValues(control);
        addView(selector);
        addedViews.add(selector);
    }

    protected void notifyChange(InteractControlBase view) {
        listener.onInteractListener(interactReply, view.getVariableName(), view.getValue());
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
