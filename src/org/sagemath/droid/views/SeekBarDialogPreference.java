package org.sagemath.droid.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import org.sagemath.droid.R;

/**
 * A {@linkplain android.preference.DialogPreference} which contains a {@linkplain android.widget.SeekBar}
 *
 * @author Nikhil Peter Raj
 */
public class SeekBarDialogPreference extends DialogPreference {

    private final int DEFAULT_VALUE = 16;

    private SeekBar fontSizeSeekBar;
    private TextView fontSizeTextView;

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.preference_seekbar);
        setDialogTitle(R.string.preference_dialog_title);
        setPositiveButtonText(R.string.ok);
        setNegativeButtonText(R.string.cancel);

    }

    @Override
    protected void onBindDialogView(View view) {

        fontSizeSeekBar = (SeekBar) view.findViewById(R.id.fontSizeSeekBar);
        fontSizeTextView = (TextView) view.findViewById(R.id.fontSizeTextView);

        fontSizeSeekBar.setMax(50);
        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fontSizeTextView.setText(progress + "sp");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setValue(getPersistedInt(DEFAULT_VALUE));

        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            persistInt(fontSizeSeekBar.getProgress());
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        int currentValue;
        if (restorePersistedValue) {
            currentValue = this.getPersistedInt(DEFAULT_VALUE);
        } else {
            currentValue = (Integer) defaultValue;
            if (shouldPersist()) {
                persistInt(currentValue);
            }
        }

    }

    private void setValue(int value) {
        fontSizeSeekBar.setProgress(value);
        fontSizeTextView.setText(value + "dp");
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        if (isPersistent()) {
            return superState;
        }

        final PreferenceSaveState saveState = new PreferenceSaveState(superState);
        saveState.value = fontSizeSeekBar.getProgress();

        return saveState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(PreferenceSaveState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        PreferenceSaveState saveState = (PreferenceSaveState) state;
        super.onRestoreInstanceState(saveState.getSuperState());

        fontSizeSeekBar.setProgress(saveState.value);
    }

    private static class PreferenceSaveState extends BaseSavedState {
        int value;

        private PreferenceSaveState(Parcel source) {
            super(source);
            value = source.readInt();
        }

        private PreferenceSaveState(Parcelable superState) {
            super(superState);
        }

        public static final Creator<PreferenceSaveState> CREATOR = new Creator<PreferenceSaveState>() {
            @Override
            public PreferenceSaveState createFromParcel(Parcel source) {
                return new PreferenceSaveState(source);
            }

            @Override
            public PreferenceSaveState[] newArray(int size) {
                return new PreferenceSaveState[size];
            }
        };

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(value);
        }
    }
}
