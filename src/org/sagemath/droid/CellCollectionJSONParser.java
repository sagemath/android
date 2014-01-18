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
import org.json.JSONTokener;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * CellCollectionJSONParser - reads and writes JSON file from/to CellData
 * 
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 *
 */
public class CellCollectionJSONParser {
	private static final String TAG = "CellCollectionJSONParser";

	private Context context;
	private String JSONfilename;
	private class SaveFileTask extends AsyncTask<JSONArray,Integer,Long> {
		protected Long doInBackground(JSONArray...arrays) {
			JSONArray array = arrays[arrays.length-1];
			Writer writer = null;
			try {
				OutputStream OS = context.openFileOutput(JSONfilename, Context.MODE_PRIVATE);
				writer = new OutputStreamWriter(OS);
				writer.write(array.toString());
				Log.i(TAG, "Cell data in JSON: " + array.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (writer != null) {
					try {
						writer.flush();
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return 0L;
		}
	}

	public CellCollectionJSONParser(Context c, String filename) {
		context = c;
		JSONfilename = filename;
	}
	
	public void saveCellData(LinkedList<CellData> cells) 
			throws Exception {
		if (cells == null || cells.isEmpty())
			throw new Exception();
		JSONArray array = new JSONArray();
		for (CellData c : cells) {
			array.put(c.toJSON());
		}
		//Log.e(TAG, "+++ saveCellData(): " + array.length() + ":" + array);
		
		new SaveFileTask().execute(array);			
	}
	
	public LinkedList<CellData> loadCells() throws Exception {
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
