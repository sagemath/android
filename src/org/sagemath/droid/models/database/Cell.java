package org.sagemath.droid.models.database;

import java.util.UUID;

/**
 * @author Haven
 */
public class Cell {

    Long _id;
    String uuid;
    String cellGroup;
    String title;
    String description;
    String input;
    int rank;
    boolean favorite;

    public Cell() {
        setUuid(UUID.randomUUID());
        //This id should be unique since UUID is inherently unique.
        _id = Long.valueOf(uuid.hashCode());
        favorite = false;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ID"+_id);
        builder.append("\t");
        builder.append("UUID:"+uuid + "\t");
        builder.append("Group:"+cellGroup + "\t");
        builder.append("Title"+title + "\t");
        builder.append("Description"+description + "\t");
        builder.append("Input"+input + "\t");
        builder.append("Rank"+rank);
        builder.append("\t");
        builder.append(favorite);

        return builder.toString();
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public UUID getUuid() {
        return UUID.fromString(uuid);
    }

    public void setUuid(UUID uuid) {
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
}
