package org.sagemath.droid;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.UUID;

import android.net.Uri;
import android.util.Log;

public class CellData {
	private final static String TAG = "CellData";
	
	protected UUID uuid;
	protected String group;
	protected String title;
	protected String description;
	protected String input;
	protected Integer rank;
	protected LinkedList<String> outputBlocks;
	
	public String getGroup() {
		return group;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getInput() {
		return input;
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
		File f = cacheDirIndexFile(output_block);
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
}
