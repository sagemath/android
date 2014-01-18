package org.sagemath.droid;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * CellCollection - Container of cells, singleton.
 * 
 * @author Rasmi Elasmar
 * @author Ralf Stephan
 *
 */
public class CellCollection {
	private static final String TAG = "CellCollection";
	private static final String JSONfilename = "celldata.json";
	
	private static CellCollection instance = new CellCollection();
	private LinkedList<CellData> data;
	private CellData currentCell;
	private Context context;
	private CellCollectionJSONParser JSONParser;
	private CellCollection() {}
	
	public static void initialize(Context context) {
		if (instance.data != null) return;
		instance.JSONParser = new CellCollectionJSONParser(context, JSONfilename);
		instance.context = context;
		instance.data = new LinkedList<CellData>();
		boolean containsData = false;
		String[] files = context.fileList();
		for (String file: files) {
			if (file.equals("celldata.json")) 
				containsData = true;
		}
		if (containsData) {
			try  {
				instance.data.addAll(instance.JSONParser.loadCells());
				Log.i(TAG, "Loaded cell data from JSON!");
			} catch (Exception e) {
				Log.e(TAG, "Couldn't load cells properly.");
			}
			
		} else {
			Log.i(TAG, "Loaded cell data from stock XML file.");
			CellCollectionXMLParser parser = new CellCollectionXMLParser();
	        InputStream ins = context.getResources().openRawResource(R.raw.cell_collection);
	        instance.data.addAll(parser.parse(ins));
		}
		
		instance.currentCell = null;
	}
	
	public void setData(LinkedList<CellData> cellData) {
		data = cellData;
	}
	
	public static CellCollection getInstance() {
		return instance;
	}
	
	public ListIterator<CellData> listIterator() {
		return data.listIterator();
	}
	
	public CellData getCurrentCell() {
		return currentCell;
	}
	
	public void setCurrentCell(CellData cell) {
		currentCell = cell;
	}
	
	public LinkedList<CellData> getCurrentGroup() {
		if (currentCell == null)
			return null;
		return getGroup(currentCell.group);
	}
	
	public String getCurrentGroupName() {
		if (currentCell == null)
			return null;
		return currentCell.group;
	}
	
	private LinkedList<String> groupsCache;
	
	public LinkedList<String> groups() {
		if (groupsCache != null)
			return groupsCache;
		LinkedList<String> g = new LinkedList<String>();
		for (CellData cell : data) {
			if (g.contains(cell.group)) continue;
			g.add(cell.group);
		}
		Collections.sort(g);
		groupsCache = g;
		return g;
	}
	
	public LinkedList<CellData> getGroup(String group) {
		LinkedList<CellData> result = new LinkedList<CellData>();
		for (CellData cell : data) 
			if (cell.group.equals(group))
				result.add(cell);
		Collections.sort(result, new CellComparator());
		return result;
	}
	
	public static class CellComparator implements Comparator<CellData> {
		@Override
		public int compare(CellData c1, CellData c2) {
			int cmp = c2.rank.compareTo(c1.rank);
			if (cmp != 0) 
				return cmp;
			return c1.title.compareToIgnoreCase(c2.title);
		}
	}
	
	public static void notifyGroupsChanged() {
		instance.groupsCache = null; 
		instance.context.sendBroadcast(new Intent().setAction("GROUPS_CHANGED"));
	}	
	
	public void addCell(CellData cell) {
		data.add(cell);
		saveCells();
		if (groupsCache == null || !groupsCache.contains(cell.group))
			notifyGroupsChanged();
	}
	
	public void removeCurrentCell() {
		String group = currentCell.group;
		data.remove(currentCell);
		saveCells();
		if (!groupsCache.contains(group))
			notifyGroupsChanged();
	}	
	
	public boolean saveCells () {
		try {
			JSONParser.saveCellData(data);
			return true;
		} catch (Exception e) {
			Log.e(TAG, "Error saving cells to JSON. " + e.getLocalizedMessage());
			return false;
		}
	}
	
	protected File getCacheDirBase() {
		return context.getCacheDir();
	}
		
}
