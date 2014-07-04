package org.sagemath.droid.interacts;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import org.sagemath.droid.models.gson.InteractReply.InteractControl;

import java.util.Arrays;

public class InteractContinuousSlider
        extends InteractControlBase
        implements OnSeekBarChangeListener {
    private final static String TAG = "SageDroid:InteractContinuousSlider";

    protected String format;
    protected SeekBar seekBar;
    protected TextView nameValueText;
    private InteractControl control;

    public InteractContinuousSlider(InteractView interactView, String variable, Context context) {
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
        seekBar.setMax(10000);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        seekBar.setLayoutParams(params);
        addView(seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        format = variable + "=%3.2f";
    }

    private double range_min = 0;
    private double range_max = 1;
    private double step = 0.1;

    public void setRange(double range_min, double range_max, double step) {
        this.range_min = range_min;
        this.range_max = range_max;
        this.step = step;
        updateValueText();
    }

    public void setRange(InteractControl control) {
        Log.i(TAG, "Setting Range:" + Arrays.toString(control.getRange()));
        this.control = control;
        this.range_min = control.getRange()[0];
        this.range_max = control.getRange()[0];
        this.step = (double) control.getStep();
        int digits = Math.max(
                countDigitsAfterComma(String.valueOf(control.getStep())),
                countDigitsAfterComma(String.valueOf(control.getRange()[0])));
        format = variable + "=%4." + digits + "f";
        updateValueText();
    }

    public Object getValue() {
        int raw = seekBar.getProgress();
        long i = Math.round(((double) raw) / seekBar.getMax() * (range_max - range_min) / step);
        double value = i * step + range_min;
        // range_max-range_min is not necessarily divisible by step
        value = Math.min(range_max, value);
        return value;
    }

    public InteractControl getViewInteractControl() {
        return control;
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }

    private void updateValueText() {
        nameValueText.setText(String.format(format, getValue()));
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
