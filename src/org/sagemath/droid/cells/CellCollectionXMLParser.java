package org.sagemath.droid.cells;


import android.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.UUID;


/**
 * @author Rasmi.Elasmar
 *
 */
public class CellCollectionXMLParser {
	private static final String TAG = "SageDroid:CellCollectionXMLParser";

	private Document dom;
	private LinkedList<CellData> data;

	
	protected void parseXML(InputStream inputStream){	
		dom = null;
		data = new LinkedList<CellData>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(inputStream);
	        dom = db.parse(is);
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "XML parse error: " + e.getLocalizedMessage());
		} catch (SAXException e) {
			Log.e(TAG, "Wrong XML file structure: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e(TAG, "I/O exeption: " + e.getLocalizedMessage());
		}      
	}

	
	public LinkedList<CellData> parse(InputStream inputStream) {
		parseXML(inputStream);
		if (dom != null)
			parseDocument();
		return data;
	}
	
	private void parseDocument(){
		Element root = dom.getDocumentElement();
		NodeList nl = root.getElementsByTagName("cell");
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {
				Element element = (Element)nl.item(i);
				CellData cell = getCellData(element);
				data.add(cell);
			}
		}
	}
	
	private CellData getCellData(Element cellElement) {
		CellData cell = new CellData();
		cell.group = getTextValue(cellElement, "group");
		cell.title = getTextValue(cellElement, "title");
		cell.description = getTextValue(cellElement, "description");
		cell.input = getTextValue(cellElement, "input");
		cell.rank = getIntValue(cellElement, "rank");
		cell.uuid = getUuidValue(cellElement, "uuid");
		cell.favorite = getBooleanValue(cellElement, "favorite");
		return cell;
	}


	private String getTextValue(Element element, String tagName) {
		String textVal = null;
		NodeList nl = element.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}
		return textVal.trim();
	}

	
	private Integer getIntValue(Element element, String tagName) {
		return Integer.parseInt(getTextValue(element, tagName));
	}
	
	private UUID getUuidValue(Element element, String tagName) {
		return UUID.fromString(getTextValue(element, tagName));
	}
	
	private Boolean getBooleanValue(Element element, String tagName) {
		return Boolean.parseBoolean(getTextValue(element, tagName));
	}

}	
