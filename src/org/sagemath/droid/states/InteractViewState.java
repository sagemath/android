package org.sagemath.droid.states;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import org.sagemath.droid.interacts.InteractContinuousSlider;
import org.sagemath.droid.interacts.InteractDiscreteSlider;
import org.sagemath.droid.interacts.InteractSelector;
import org.sagemath.droid.models.gson.InteractReply.InteractControl;

import java.util.ArrayList;

/**
 * Created by Haven on 02-07-2014.
 */
public class InteractViewState extends View.BaseSavedState {

    private ArrayList<View> addedViews;
    private ArrayList<InteractControl> savedControls;

    public InteractViewState(Parcelable superState
            , ArrayList<View> addedViews) {
        super(superState);
        this.addedViews = addedViews;
        saveViewData();
    }

    private void saveViewData() {
        savedControls = new ArrayList<InteractControl>();
        InteractControl control = null;
        for (View v : addedViews) {
            if (v instanceof InteractContinuousSlider) {
                InteractContinuousSlider slider = (InteractContinuousSlider) v;
                control = slider.getViewInteractControl();
                control.setIntSavedValue(slider.getSeekBar().getProgress());
                control.setViewEnabled(slider.getSeekBar().isEnabled());

            } else if (v instanceof InteractDiscreteSlider) {
                InteractDiscreteSlider slider = (InteractDiscreteSlider) v;
                control = slider.getViewInteractControl();
                control.setIntSavedValue(slider.getSeekBar().getProgress());
                control.setViewEnabled(slider.getSeekBar().isEnabled());
            } else if (v instanceof InteractSelector) {
                InteractSelector selector = (InteractSelector) v;
                control = selector.getViewInteractControl();
                control.setStringSavedValue(selector.getSpinner().getSelectedItem().toString());
                control.setViewEnabled(selector.getSpinner().isEnabled());
            }
            if (control != null) {
                savedControls.add(control);
            }
        }
    }

    public ArrayList<InteractControl> getSavedControls() {
        return savedControls;
    }

    private InteractViewState(Parcel source) {
        super(source);
        savedControls = (ArrayList<InteractControl>) source.readSerializable();
    }

    public static final Creator<InteractViewState> CREATOR = new Creator<InteractViewState>() {
        @Override
        public InteractViewState createFromParcel(Parcel source) {
            return new InteractViewState(source);
        }

        @Override
        public InteractViewState[] newArray(int size) {
            return new InteractViewState[size];
        }
    };

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeSerializable(savedControls);
    }
}
