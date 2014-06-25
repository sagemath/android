package org.sagemath.droid.interacts;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import org.sagemath.droid.models.gson.InteractReply.InteractControl;

import java.util.Arrays;
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
                nameValueText.getPaddingLeft() + 10,
                nameValueText.getPaddingTop() + 5,
                nameValueText.getPaddingRight() + 5,
                nameValueText.getPaddingBottom());
        addView(nameValueText);

        spinner = new Spinner(context);
        addView(spinner);
        spinner.setOnItemSelectedListener(this);
        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, values);
        spinner.setAdapter(adapter);
    }

    private LinkedList<String> values = new LinkedList<String>();

    public void setValues(InteractControl control) {
        Log.i(TAG, "Setting Values: " + Arrays.toString(control.getValueLabels()));
        values.clear();
        for (String i : control.getValueLabels()) {
            values.add(i);
        }
        adapter.notifyDataSetChanged();
        currentSelection = 0;
        spinner.setSelection(0);
        updateValueText();
    }

    public Integer getValue() {
        return spinner.getSelectedItemPosition();
    }

    public Spinner getSpinner() {
        return spinner;
    }

    private void updateValueText() {
        if (values.isEmpty() || getValue() == -1) return;
        Log.e(TAG, "value = " + getValue());
        nameValueText.setText(getVariableName() + ":");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {
        if (currentSelection == position)
            return;
        currentSelection = position;
        Log.e(TAG, "selected " + position);
        updateValueText();
        interactView.notifyChange(this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

}
