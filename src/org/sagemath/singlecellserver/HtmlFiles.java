package org.sagemath.singlecellserver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HtmlFiles extends CommandOutput {
	private final static String TAG = "HtmlFiles"; 

	protected JSONArray files;
	protected LinkedList<URI> uriList = new LinkedList<URI>();
	
	protected HtmlFiles(JSONObject json) throws JSONException {
		super(json);
		files = json.getJSONObject("content").getJSONObject("content").getJSONArray("files");
	}

	public String toString() {
		if (uriList.isEmpty())
			return "Html files (empty list)";
		else
			return "Html files, number = "+uriList.size()+" first = "+getFirstURI().toString();
	}
			
	public URI getFirstURI() {
		return uriList.getFirst();
	}

	public void downloadFile(SageSingleCell.ServerTask server) 
			throws IOException, URISyntaxException, JSONException {
		for (int i=0; i<files.length(); i++) {
			URI uri = server.downloadFileURI(this, files.get(i).toString());
			uriList.add(uri);
		}
	}

	
}
