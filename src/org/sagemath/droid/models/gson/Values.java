package org.sagemath.droid.models.gson;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Haven
 */
public class Values implements Parcelable {

    private int intValue;
    private String[] values;

    public Values() {

    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    private Values(Parcel in) {
        intValue = in.readInt();
        values = in.createStringArray();
    }

    public static final Creator<Values> CREATOR = new Creator<Values>() {
        @Override
        public Values createFromParcel(Parcel source) {
            return new Values(source);
        }

        @Override
        public Values[] newArray(int size) {
            return new Values[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(intValue);
        dest.writeStringArray(values);
    }
}
