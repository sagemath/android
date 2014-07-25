package org.sagemath.droid.models.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Haven on 19-07-2014.
 */
public class Group implements Parcelable {
    private static final String TAG = "SageDroid:Groups";

    Long _id;
    String cellGroup;

    public Group() {

    }

    public Long getId() {
        return _id;
    }

    public void setId(Long _id) {
        this._id = _id;
    }

    public String getCellGroup() {
        return cellGroup;
    }

    public void setCellGroup(String cellGroup) {
        this.cellGroup = cellGroup;
    }

    private Group(Parcel in) {
        _id = in.readLong();
        cellGroup = in.readString();
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel source) {
            return new Group(source);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(cellGroup);
    }
}
