package org.sagemath.droid.cells;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.UUID;

/**
 * CellData - one cell
 *
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 */

public class CellData implements Parcelable {
    private static final String TAG = "SageDroid:CellData";
    private static final String JSON_UUID = "uuid";
    private static final String JSON_GROUP = "group";
    private static final String JSON_TITLE = "title";
    private static final String JSON_DESCRIPTION = "description";
    private static final String JSON_INPUT = "input";
    private static final String JSON_RANK = "rank";
    private static final String JSON_FAVORITE = "favorite";
    private static final String JSON_HTML = "htmlData";

    //TODO Make all of this private so we can use the getter and setter methods everywhere now that everything is in packages
    protected UUID uuid;
    protected String group;
    protected String title;
    protected String description;
    protected String input;
    protected Integer rank;
    protected Boolean favorite;
    protected String htmlData;
    protected LinkedList<String> outputBlocks;

    public CellData() {
        uuid = UUID.randomUUID();
        favorite = false;
        htmlData = "";
    }

    public CellData(CellData originalCell) {
        uuid = UUID.randomUUID();
        group = originalCell.group;
        title = originalCell.title;
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm aaa", Locale.US);
        description = dateFormat.format(date);
        input = originalCell.input;
        rank = originalCell.rank;
        favorite = originalCell.favorite;

        htmlData = originalCell.htmlData;

        saveOutput(uuid.toString(), htmlData);
    }

    public CellData(JSONObject json) throws JSONException {

        uuid = UUID.fromString(json.getString(JSON_UUID));
        group = json.getString(JSON_GROUP);
        title = json.getString(JSON_TITLE);
        description = json.optString(JSON_DESCRIPTION);
        input = json.getString(JSON_INPUT);
        rank = json.getInt(JSON_RANK);
        favorite = json.getBoolean(JSON_FAVORITE);
        htmlData = json.getString(JSON_HTML);

        if (description == null) {
            Log.e(TAG, "Null description in CellData. Fixed.");
            description = "";
        }
    }

    //Internal constructor for createFromParcel()
    private CellData(Parcel in) {
        uuid = UUID.fromString(in.readString());
        group = in.readString();
        title = in.readString();
        description = in.readString();
        input = in.readString();
        rank = in.readInt();
        favorite = in.readByte() != 0; // true if !=0,false otherwise
        htmlData = in.readString();

        //Unsure is saveOutput is needed
    }

    public static final Creator<CellData> CREATOR =
            new Creator<CellData>() {
                @Override
                public CellData createFromParcel(Parcel source) {
                    return new CellData(source);
                }

                @Override
                public CellData[] newArray(int size) {
                    return new CellData[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid.toString());
        dest.writeString(group);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(input);
        dest.writeInt(rank);
        dest.writeByte((byte) (favorite ? 1 : 0));
        dest.writeString(htmlData);

    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public void updateGroup(String group) {
        if (!this.group.equals(group)) {
            CellCollection.notifyGroupsChanged();
            this.group = group;
        }
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

    public void setInput(String str) {
        this.input = str;
    }

    public Boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getHtmlData() {
        return htmlData;
    }

    private File cache;

    public File cacheDir() {
        if (cache != null)
            return cache;
        File base = CellCollection.getInstance().getCacheDirBase();
        cache = new File(base, uuid.toString());
        if (!cache.exists()) {
            boolean rc = cache.mkdir();
            if (!rc)
                Log.e(TAG, "Unable to create directory " + cache.getAbsolutePath());
        }
        return cache;
    }

    protected File cacheDirIndexFile(String output_block) {
        return new File(cacheDir(), output_block + ".html");
    }

    public void saveOutput(String output_block, String html) {
        htmlData += html;
        Log.i(TAG, "CellData added output_block to " + title + " " + uuid.toString() + ": " + html);
        File f = cacheDirIndexFile(output_block);
        Log.i(TAG, "Saving html: " + output_block + " " + html);
        FileOutputStream os;
        try {
            os = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to save output: " + e.getLocalizedMessage());
            return;
        }

        try {
            os.write(html.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Unable to save output: " + e.getLocalizedMessage());
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                Log.e(TAG, "Unable to save output: " + e.getLocalizedMessage());
            }
        }

    }

    public String getUrlString(String block) {
        File file = cacheDirIndexFile(block);
        if (file.exists()) {
            Uri uri = Uri.fromFile(file);
            Log.i(TAG, "Returning URL String: " + uri.toString());
            return uri.toString();
        }

        return null;
    }

    public boolean hasCachedOutput(String block) {
        return cacheDirIndexFile(block).exists();
    }

    public void clearCache() {
        File[] files = cacheDir().listFiles();
        for (File file : files)
            if (!file.delete())
                Log.e(TAG, "Error deleting " + file);
    }

    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(JSON_UUID, uuid.toString());
        json.put(JSON_GROUP, group);
        json.put(JSON_TITLE, title);
        json.put(JSON_DESCRIPTION, description);
        json.put(JSON_INPUT, input);
        json.put(JSON_RANK, rank);
        json.put(JSON_FAVORITE, favorite);
        json.put(JSON_HTML, htmlData);

        return json;
    }

}
