package org.sagemath.droid.interacts;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class InteractSelector 
		extends InteractControlBase
		implements OnItemSelectedListener {
	private final static String TAG = "SageDroid:InteractSelector";
	
	protected Spinner spinner;
	protected ArrayAdapter<String> adapter;
	protected TextView nameValueText;
	protected int currentSelection = 0;
	
	public InteractSelector(InteractView interactView, String variable, Context context) {
		super(interactView, variable, context);
		
		nameValueText = new TextView(context);
		nameValueText.setMaxLines(1);
		nameValueText.setPadding(
				nameValueText.getPaddingLeft()+10, 
				nameValueText.getPaddingTop()+5, 
				nameValueText.getPaddingRight()+5, 
				nameValueText.getPaddingBottom());
		addView(nameValueText);
		
		spinner = new Spinner(context);
		addView(spinner);
		spinner.setOnItemSelectedListener(this);
		adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, values);
		spinner.setAdapter(adapter);
	}
	
	private LinkedList<String> values = new LinkedList<String>();
	
	public void setValues(LinkedList<String> values) {
		this.values = values;
		adapter.notifyDataSetChanged();
		currentSelection = 0;
		spinner.setSelection(0);
		updateValueText();
	}

	public void setValues(JSONObject control) {
		this.values.clear();
		try {
			JSONArray values= control.getJSONArray("value_labels");
			for (int i=0; i<values.length(); i++)
				this.values.add( values.getString(i) );
				
		} catch (JSONException e) {
			Log.e(TAG, e.getLocalizedMessage());
			this.values.add("0");
			this.values.add("1");
		}
		adapter.notifyDataSetChanged();
		currentSelection = 0;
		spinner.setSelection(0);
		updateValueText();
	}

	public Integer getValue() {
		int raw = spinner.getSelectedItemPosition();
		return raw;
	}
	
	private void updateValueText() {
		if (values.isEmpty() || getValue()==-1) return;
		Log.e(TAG, "value = "+getValue());
		nameValueText.setText(getVariableName() + ":");		
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {
		if (currentSelection == position) 
			return;
		currentSelection = position;
		Log.e(TAG, "selected "+position);
		updateValueText();
		interactView.notifyChange(this);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

}
