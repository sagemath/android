package org.sagemath.droid.interacts;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.singlecellserver.Interact;

import android.content.Context;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;

public class InteractView extends TableLayout {
	private final static String TAG = "InteractView";
	
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
		public void onInteractListener(Interact interact, String name, Object value);
	}
	
	private OnInteractListener listener;
	
	public void setOnInteractListener(OnInteractListener listener) {
		this.listener = listener;
	}
	
	private Interact interact;
	private JSONArray layout;
	
	private List<String> layoutPositions = Arrays.asList(
			"top_left", "top_center", "top_right", "left", "right",
			"bottom_left", "bottom_center", "bottom_right");
	
	public void set(Interact interact) {
		// Log.e(TAG, "set "+interact.toShortString());
		this.interact = interact;
		removeAllViews();
		JSONArray layout = interact.getLayout();

		int i = 0;
		while (i < layout.length()) {
			JSONArray variables;
			try {
				variables = layout.getJSONArray(i);
				addInteract(interact, variables.getJSONArray(0).getString(0));
				//Log.i(TAG, "variables.toString() " + variables.toString());
			} catch (JSONException e) {}
			i++;
		}
	}
	
	public void addInteract(Interact interact, String variable) {
		JSONObject controls = interact.getControls();
		try {
			JSONObject control = controls.getJSONObject(variable);
			String control_type = control.getString("control_type");
			if (control_type.equals("slider")) {
				//Log.i(TAG, "Control type is slider.");
				String subtype = control.getString("subtype");
				if (subtype.equals("discrete")){
					//Log.i(TAG,"Subtype is discrete slider.");
					addDiscreteSlider(variable, control);
				} else if (subtype.equals("continuous")) {
					//Log.i(TAG,"Subtype is continuous slider.");
					addContinuousSlider(variable, control);
				} else	
					Log.e(TAG, "Unknown slider type: "+subtype);
			} else if (control_type.equals("selector")) {
				//Log.i(TAG, "Control type is selector.");
				addSelector(variable, control);
			}		
		} catch (JSONException e) {
			Log.e(TAG, e.getLocalizedMessage());
		}
	}

	protected void addContinuousSlider(String variable, JSONObject control) throws JSONException {
		InteractContinuousSlider slider = new InteractContinuousSlider(this, variable, context);
		slider.setRange(control);
		addView(slider);
		//Log.i(TAG, "Added Continuous Slider view!");
	}
	
	protected void addDiscreteSlider(String variable, JSONObject control) throws JSONException {
		InteractDiscreteSlider slider = new InteractDiscreteSlider(this, variable, context);
		slider.setValues(control);
		addView(slider);
		//Log.i(TAG, "Added Discrete Slider view!");
	}
	
	protected void addSelector(String variable, JSONObject control) throws JSONException {
		InteractSelector selector = new InteractSelector(this, variable, context);
		selector.setValues(control);
		addView(selector);
		//Log.i(TAG, "Added Selector view!");
	}

	
	protected void notifyChange(InteractControlBase view) {
		//Log.i(TAG, "InteractView value: " + view.getVariableName() + " = " + view.getValue());
		listener.onInteractListener(interact, view.getVariableName(), view.getValue());
	}
	
}
