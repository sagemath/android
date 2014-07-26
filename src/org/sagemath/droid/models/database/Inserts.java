package org.sagemath.droid.models.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Nikhil Peter Raj
 */
public class Inserts implements Parcelable {
    private static final String TAG = "SageDroid:Inserts";

    Long _id;
    String insertDescription;
    String insertText;
    boolean isFavorite;

    public Inserts() {

    }

    public Inserts(long _id) {
        this._id = _id;
    }

    public Long getId() {
        return _id;
    }

    public String getInsertDescription() {
        return insertDescription;
    }

    public void setInsertDescription(String insertDescription) {
        this.insertDescription = insertDescription;
    }

    public String getInsertText() {
        return insertText;
    }

    public void setInsertText(String insertText) {
        this.insertText = insertText;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    @Override
    public String toString() {
        //IntelliJ bugs -_-
        return ((Object) this).getClass().getName() + "[" +
                "_id=" + _id + ", " +
                "insertDescription=" + insertDescription + ", " +
                "insertText=" + insertText + ", " +
                "isFavorite=" + isFavorite + "," + "]";
    }

    private Inserts(Parcel in) {
        _id = in.readLong();
        insertDescription = in.readString();
        insertText = in.readString();
        isFavorite = in.readInt() == 1;
    }

    public static final Creator<Inserts> CREATOR = new Creator<Inserts>() {
        @Override
        public Inserts createFromParcel(Parcel source) {
            return new Inserts(source);
        }

        @Override
        public Inserts[] newArray(int size) {
            return new Inserts[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(insertDescription);
        dest.writeString(insertText);
        dest.writeInt(isFavorite ? 1 : 0);
    }
}
