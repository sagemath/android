package org.sagemath.singlecellserver;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.singlecellserver.SageSingleCell.SageInterruptedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class DataFile extends DisplayData {
	private final static String TAG = "SageDroid:DataFile";
	protected byte[] data;
	protected URI uri;
	
	protected DataFile(JSONObject json) throws JSONException {
		super(json);
		Log.i(TAG, "Created new DataFile!");
	}


	public String toString() {
		if (data == null)
			return "Data file "+uri.toString();
		else
			return "Data file "+value+" ("+data.length+" bytes)";
	}

	public String mime() {
		String name = value.toLowerCase();
		if (name.endsWith(".png")) 	return "image/png";
		if (name.endsWith(".jpg")) 	return "image/png";
		if (name.endsWith(".jpeg"))	return "image/png";
		if (name.endsWith(".svg")) 	return "image/svg";
		return null;
	}
	

	
	public URI getURI() {
		Log.i(TAG, "DataFile.getURI() called");
		return uri;
	}
	
	public void downloadFile(SageSingleCell.ServerTask server) 
			throws IOException, URISyntaxException, SageInterruptedException {
		Log.i(TAG, "DataFile.downloadFile called.");
		uri = server.downloadFileURI(this, this.value);
		if (server.downloadDataFiles())
			download(server, uri);
	}

	private void download(SageSingleCell.ServerTask server, URI uri) 
			throws IOException, SageInterruptedException {
    	HttpResponse response = server.downloadFile(uri);
    	boolean error = (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK);
        HttpEntity entity = response.getEntity();
        InputStream stream = entity.getContent();

        byte[] buffer = new byte[4096];  
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {  
            for (int n; (n = stream.read(buffer)) != -1; )   
                buf.write(buffer, 0, n);  
        } finally { 
        	stream.close();
        	buf.close();
        }
        data = buf.toByteArray();
	}

}
