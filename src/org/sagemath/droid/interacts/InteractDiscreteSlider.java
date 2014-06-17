package org.sagemath.droid.interacts;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.droid.models.InteractReply.InteractControl;

import java.util.Arrays;
import java.util.LinkedList;

public class InteractDiscreteSlider
        extends InteractControlBase
        implements OnSeekBarChangeListener {
    private final static String TAG = "SageDroid:InteractDiscreteSlider";

    protected SeekBar seekBar;
    protected TextView nameValueText;

    public InteractDiscreteSlider(InteractView interactView, String variable, Context context) {
        super(interactView, variable, context);

        nameValueText = new TextView(context);
        nameValueText.setMaxLines(1);
        nameValueText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f));
        nameValueText.setPadding(
                nameValueText.getPaddingLeft() + 10,
                nameValueText.getPaddingTop() + 5,
                nameValueText.getPaddingRight() + 5,
                nameValueText.getPaddingBottom());
        addView(nameValueText);

        seekBar = new SeekBar(context);
        seekBar.setMax(1);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        seekBar.setLayoutParams(params);
        addView(seekBar);
        seekBar.setOnSeekBarChangeListener(this);
    }

    private LinkedList<String> values = new LinkedList<String>();

    public void setValues(LinkedList<String> values) {
        this.values = values;
        seekBar.setMax(this.values.size() - 1);
        updateValueText();
    }

    public void setValues(JSONObject control) {
        this.values.clear();
        try {
            JSONArray values = control.getJSONArray("values");
            //Log.i(TAG, "SLIDER VALUES: " + values.toString());
            for (int i = 0; i < values.length(); i++) {
                //Log.i(TAG, "Adding value: " + values.getString(i));
                this.values.add(values.getString(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
            this.values.add("0");
            this.values.add("1");
        }
        seekBar.setMax(this.values.size() - 1);
        updateValueText();
    }

    public void setValues(InteractControl control) {
        Log.i(TAG, "Setting Values" + Arrays.toString(control.getValues()));
        this.values.clear();
        for (String i : control.getValues()) {
            values.add(i);
        }
        seekBar.setMax(values.size()-1);
        updateValueText();
    }

    public Integer getValue() {
        int index = seekBar.getProgress();
        return index;
    }

    private void updateValueText() {
        nameValueText.setText(getVariableName() + "=" + values.get(getValue()));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        updateValueText();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        interactView.notifyChange(this);
    }
}
