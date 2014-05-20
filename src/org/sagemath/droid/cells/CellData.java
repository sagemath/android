package org.sagemath.droid.cells;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

/**
 * CellData - one cell
 * 
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 *
 */

public class CellData {
	private static final String TAG = "CellData";
	private static final String JSON_UUID = "uuid";
	private static final String JSON_GROUP = "group";
	private static final String JSON_TITLE= "title";
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
		DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm aaa",Locale.US);
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

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
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
				Log.e(TAG, "Unable to create directory "+cache.getAbsolutePath());
		}
		return cache;
	}

	protected File cacheDirIndexFile(String output_block) {
		addOutputBlock(output_block);
		return new File(cacheDir(), output_block + ".html");
	}

	public void saveOutput(String output_block, String html) {
		addOutputBlock(output_block);
		htmlData += html;
		Log.i(TAG, "CellData added output_block to " + title + " " + uuid.toString() + ": "+ html);
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
		Uri uri = Uri.fromFile(cacheDirIndexFile(block));
		return uri.toString();
	}

	public boolean hasCachedOutput(String block) {
		return cacheDirIndexFile(block).exists();
	}

	public void clearCache() {
		File[] files = cacheDir().listFiles();
		for (File file : files)
			if (!file.delete())
				Log.e(TAG, "Error deleting "+file);
	}

	private void addOutputBlock(String block) {
		//Log.i(TAG, "addOutputBlock: " + block);
		if (outputBlocks == null) 
			outputBlocks = new LinkedList<String>();
		if (!outputBlocks.contains(block)) {
			outputBlocks.add(block);
			saveOutputBlocks();
		}
	}

	private void saveOutputBlocks() {
		File file = new File(cacheDir(), "output_blocks");
		try {
			saveOutputBlocks(file);
		} catch (IOException e) {
			Log.e(TAG, "Unable to save output_block list: "+e.getLocalizedMessage());
		} 
	}

	private void saveOutputBlocks(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeInt(outputBlocks.size());
		for (String block : outputBlocks) {
			dos.writeUTF(block);
		}
		// Log.e(TAG, "saved "+outputBlocks.size()+" output_block fields");
		dos.close();
	}

	private LinkedList<String> loadOutputBlocks(File file) throws IOException {
		LinkedList<String> result = new LinkedList<String>();
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		DataInputStream dis = new DataInputStream(bis);
		int n = dis.readInt();
		for (int i=0; i<n; i++) {
			String block = dis.readUTF();
		}
		// Log.e(TAG, "read "+n+" output_block fields");
		dis.close();
		return result;
	}

	public LinkedList<String> getOutputBlocks() {
		if (outputBlocks != null)
			return outputBlocks;
		outputBlocks = new LinkedList<String>();
		File file = new File(cacheDir(), "output_blocks");
		if (!file.exists()) return outputBlocks;
		try {
			outputBlocks.addAll(loadOutputBlocks(file));
		} catch (IOException e) {
			Log.e(TAG, "Unable to load output_block list: "+e.getLocalizedMessage());
		}
		return outputBlocks;
	}

	public JSONObject toJSON() throws JSONException {

		JSONObject json = new JSONObject();
		json.put(JSON_UUID, uuid.toString());
		json.put(JSON_GROUP, group);
		json.put(JSON_TITLE,  title);
		json.put(JSON_DESCRIPTION, description);
		json.put(JSON_INPUT, input);
		json.put(JSON_RANK, rank);
		json.put(JSON_FAVORITE, favorite);
		json.put(JSON_HTML, htmlData);

		return json;
	}


}
