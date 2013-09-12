package org.sagemath.droid;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.util.Log;


public class CellCollectionJSONParser {
	private static final String TAG = "CellCollectionJSONParser";

	private Context context;
	private String JSONfilename;
	
	public CellCollectionJSONParser(Context c, String filename) {
		context = c;
		JSONfilename = filename;
	}
	
	public void saveCellData(LinkedList<CellData> cells) 
			throws JSONException, IOException {
		JSONArray array = new JSONArray();
		for (CellData c : cells) {
			array.put(c.toJSON());
		}
			
		Writer writer = null;
		try {
			OutputStream OS = context.openFileOutput(JSONfilename, Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(OS);
			writer.write(array.toString());
			Log.i(TAG, "Cell data in JSON: " + array.toString());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	public LinkedList<CellData> loadCells() throws IOException, JSONException {
		LinkedList<CellData> cells = new LinkedList<CellData>();
		BufferedReader reader = null;
		try {
			InputStream IS = context.openFileInput(JSONfilename);
			reader = new BufferedReader(new InputStreamReader(IS));
			StringBuilder jsonString = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				jsonString.append(line);
			}
			JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
			for (int i = 0; i < array.length(); i++) {
				cells.add(new CellData(array.getJSONObject(i)));
			}
		} catch (Exception e) {
			Log.e(TAG, "Issues when loading cell data from JSON." + e.getLocalizedMessage());
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		
		return cells;
	}
	

	
	

}	
