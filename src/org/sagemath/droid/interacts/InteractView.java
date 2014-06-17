package org.sagemath.droid.interacts;

import android.content.Context;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.droid.constants.ControlType;
import org.sagemath.droid.models.InteractReply;
import org.sagemath.droid.models.InteractReply.InteractControl;
import org.sagemath.droid.models.InteractReply.SageInteract;
import org.sagemath.singlecellserver.Interact;

import java.util.Arrays;
import java.util.List;

public class InteractView extends TableLayout {
    private final static String TAG = "SageDroid:InteractView";

    private Context context;

    private TableRow row_top, row_center, row_bottom;

    public InteractView(Context context) {
        super(context);
        this.context = context;
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

    private Interact interact;
    private InteractReply interactReply;
    private JSONArray layout;

    private List<String> layoutPositions = Arrays.asList(
            "top_left", "top_center", "top_right", "left", "right",
            "bottom_left", "bottom_center", "bottom_right");

    public void set(Interact interact) {
        // Log.e(TAG, "set "+updateInteract.toShortString());
        this.interact = interact;
        removeAllViews();
        JSONArray layout = interact.getLayout();
        Log.i(TAG, "Layout" + layout.toString());

        int i = 0;
        while (i < layout.length()) {
            JSONArray variables;
            try {
                variables = layout.getJSONArray(i);
                addInteract(interact, variables.getJSONArray(0).getString(0));
                Log.i(TAG, "Current Variable: " + variables.toString());
                Log.i(TAG, "Current VarArray: " + variables.getJSONArray(0).toString());
                Log.i(TAG, "Current Key: " + variables.getJSONArray(0).getString(0));

            } catch (JSONException e) {
            }
            i++;
        }
    }

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

    public void addInteract(Interact interact, String variable) {
        JSONObject controls = interact.getControls();
        try {
            JSONObject control = controls.getJSONObject(variable);
            String control_type = control.getString("control_type");
            if (control_type.equals("slider")) {
                Log.i(TAG, "Control type is slider.");
                String subtype = control.getString("subtype");
                if (subtype.equals("discrete")) {
                    Log.i(TAG, "Subtype is discrete slider.");
                    addDiscreteSlider(variable, control);
                } else if (subtype.equals("continuous")) {
                    Log.i(TAG, "Subtype is continuous slider.");
                    addContinuousSlider(variable, control);
                } else
                    Log.e(TAG, "Unknown slider type: " + subtype);
            } else if (control_type.equals("selector")) {
                Log.i(TAG, "Control type is selector.");
                addSelector(variable, control);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
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

    protected void addContinuousSlider(String variable, JSONObject control) throws JSONException {
        InteractContinuousSlider slider = new InteractContinuousSlider(this, variable, context);
        slider.setRange(control);
        addView(slider);
        //Log.i(TAG, "Added Continuous Slider view!");
    }

    protected void addContinuousSlider(InteractControl control) {
        Log.i(TAG, "Adding Continous Slider");
        InteractContinuousSlider slider = new InteractContinuousSlider(this, control.getVarName(), context);
        slider.setRange(control);
        addView(slider);
    }

    protected void addDiscreteSlider(String variable, JSONObject control) throws JSONException {
        InteractDiscreteSlider slider = new InteractDiscreteSlider(this, variable, context);
        slider.setValues(control);
        addView(slider);
        //Log.i(TAG, "Added Discrete Slider view!");
    }

    protected void addDiscreteSlider(InteractControl control) {
        Log.i(TAG, "Adding Discrete Slider");
        InteractDiscreteSlider slider = new InteractDiscreteSlider(this, control.getVarName(), context);
        slider.setValues(control);
        addView(slider);
    }

    protected void addSelector(String variable, JSONObject control) throws JSONException {
        InteractSelector selector = new InteractSelector(this, variable, context);
        selector.setValues(control);
        addView(selector);
        //Log.i(TAG, "Added Selector view!");
    }

    protected void addSelector(InteractControl control) {
        Log.i(TAG, "Adding a selector");
        InteractSelector selector = new InteractSelector(this, control.getVarName(), context);
        selector.setValues(control);
    }

    protected void notifyChange(InteractControlBase view) {
        listener.onInteractListener(interactReply, view.getVariableName(), view.getValue());
    }

}
