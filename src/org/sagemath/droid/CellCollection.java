package org.sagemath.droid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

public class CellCollection {
	private final static String TAG = "CellCollection";
	
	private static CellCollection instance = new CellCollection();
	private LinkedList<CellData> data;
	private CellData current;
	private Context context;
	private CellCollection() {}
	
	public static void initialize(Context context) {
		if (instance.data != null) return;
		instance.context = context;
		instance.data = new LinkedList<CellData>();
		CellCollectionXMLParser parser = new CellCollectionXMLParser();
        InputStream ins = context.getResources().openRawResource(R.raw.cell_collection);
        instance.data.addAll(parser.parse(ins));
        instance.current = instance.getGroup("My Worksheets").getFirst();
	}
	
	public static CellCollection getInstance() {
		return instance;
	}
	
	public ListIterator<CellData> listIterator() {
		return data.listIterator();
	}
	
	public CellData getCurrentCell() {
		return current;
	}
	
	public void setCurrentCell(CellData cell) {
		current = cell;
	}
	
	public LinkedList<CellData> getCurrentGroup() {
		return getGroup(current.group);
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
	
	public void addCell(CellData cell) {
		data.add(cell);
	}
	
	protected File getCacheDirBase() {
		return context.getCacheDir();
	}
		
}
