package org.sagemath.droid.models.database;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;
import java.util.UUID;

/**
 * @author Nikhil Peter Raj
 */
public class Cell implements Parcelable {

    Long _id;
    String uuid;
    String cellGroup;
    String title;
    String description;
    String input;
    int rank;
    boolean favorite;

    public Cell() {
        setUUID(UUID.randomUUID());
        //This id should be unique since UUID is inherently unique.
        _id = Long.valueOf(uuid.hashCode());
        favorite = false;
    }

    //---For Parcelable---

    private Cell(Parcel in) {
        _id = in.readLong();
        uuid = in.readString();
        cellGroup = in.readString();
        title = in.readString();
        description = in.readString();
        input = in.readString();
        rank = in.readInt();
        favorite = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(uuid);
        dest.writeString(cellGroup);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(input);
        dest.writeInt(rank);
        dest.writeByte((byte) (favorite ? 1 : 0));
    }

    public static final Creator<Cell> CREATOR = new Creator<Cell>() {
        @Override
        public Cell createFromParcel(Parcel source) {
            return new Cell(source);
        }

        @Override
        public Cell[] newArray(int size) {
            return new Cell[size];
        }
    };

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ID" + _id);
        builder.append("\t");
        builder.append("UUID:" + uuid + "\t");
        builder.append("Group:" + cellGroup + "\t");
        builder.append("Title" + title + "\t");
        builder.append("Description" + description + "\t");
        builder.append("Input" + input + "\t");
        builder.append("Rank" + rank);
        builder.append("\t");
        builder.append(favorite);

        return builder.toString();
    }

    //--Setters & Getters

    public long getID() {
        return _id;
    }

    public void setID(long _id) {
        this._id = _id;
    }

    public UUID getUUID() {
        return UUID.fromString(uuid);
    }

    public void setUUID(UUID uuid) {
        this.uuid = uuid.toString();
    }

    public String getGroup() {
        return cellGroup;
    }

    public void setGroup(String cellGroup) {
        this.cellGroup = cellGroup;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public static class CellComparator implements Comparator<Cell> {
        @Override
        public int compare(Cell lhs, Cell rhs) {
            int cmp = Boolean.valueOf(lhs.isFavorite())
                    .compareTo(rhs.isFavorite());
            if (cmp != 0)
                return cmp;
            cmp = Integer.valueOf(rhs.getRank()).compareTo(lhs.getRank());
            if (cmp != 0) {
                return cmp;
            }
            return lhs.getTitle().compareToIgnoreCase(rhs.getTitle());
        }
    }
}
