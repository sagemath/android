package org.sagemath.droid.states;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

/**
 * @author Nikhil Peter Raj
 */
public class OutputViewState extends View.BaseSavedState {


    private OutputBlockState blockState = null;
    private InteractViewState viewState = null;

    public OutputViewState(Parcel source) {

        super(source);
        blockState = source.readParcelable(OutputBlockState.class.getClassLoader());
        viewState = source.readParcelable(InteractViewState.class.getClassLoader());
    }

    public OutputViewState(Parcelable superState, OutputBlockState blockState, InteractViewState viewState) {
        super(superState);

        this.blockState = blockState;
        this.viewState = viewState;
    }

    public OutputBlockState getOutputBlockState() {
        return blockState;
    }

    public InteractViewState getInteractViewState() {
        return viewState;
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(blockState, flags);
        dest.writeParcelable(viewState, flags);
    }
}
