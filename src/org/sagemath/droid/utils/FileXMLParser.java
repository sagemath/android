package org.sagemath.droid.utils;


import android.util.Log;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.models.database.Group;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


/**
 * @author Rasmi.Elasmar
 * @author Nikhil Peter Raj
 */
public class FileXMLParser {
    private static final String TAG = "SageDroid:CellCollectionXMLParser";

    private Document dom;
    private List<Cell> cells;
    private List<Group> groups;


    void parseXML(InputStream inputStream) {
        dom = null;
        cells = new LinkedList<>();
        groups = new ArrayList<>();
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

    public void parse(InputStream inputStream) {
        parseXML(inputStream);
        if (dom != null)
            parseDocument();
    }

    public List<Cell> getIntitalCells() {
        return cells;
    }

    public List<Group> getInitialGroups() {
        return groups;
    }

    private void parseDocument() {
        Element root = dom.getDocumentElement();
        NodeList nl = root.getElementsByTagName("cell");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element element = (Element) nl.item(i);
                Cell cell = getCellData(element);
                cells.add(cell);
            }
        }
    }

    private Cell getCellData(Element cellElement) {
        Cell cell = new Cell();
        cell.setGroup(getGroup(cellElement, "group"));
        cell.setTitle(getTextValue(cellElement, "title"));
        cell.setDescription(getTextValue(cellElement, "description"));
        cell.setInput(getTextValue(cellElement, "input"));
        cell.setRank(getIntValue(cellElement, "rank"));
        cell.setUUID(getUuidValue(cellElement, "uuid"));
        cell.setFavorite(getBooleanValue(cellElement, "favorite"));
        return cell;
    }

    private Group getGroup(Element element, String tagName) {
        Group group = null;
        NodeList nl = element.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            group = new Group();
            Element el = (Element) nl.item(0);
            group.setCellGroup(el.getFirstChild().getNodeValue());
            if (!groups.contains(group))
                groups.add(group);
        }
        return group;
    }


    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nl = element.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
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
