package org.sagemath.droid;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import org.sagemath.droid.cells.CellData;
import org.sagemath.singlecellserver.*;

import java.util.LinkedList;
import java.util.ListIterator;

public class OutputBlock extends WebView {
	private final static String TAG = "SageDroid:OutputBlock";

	private final CellData cell;
	private LinkedList<String> divs = new LinkedList<String>();
	
	public OutputBlock(Context context, CellData cell) {
		super(context);
		this.cell = cell;
	}
	
	public OutputBlock(Context context, CellData cell, String htmlData) {
		super(context);
		Log.i(TAG, "Created outputblock from htmldata.");
		this.cell = cell;
		divs.clear();
		divs.add(htmlData);
		try {
		Log.i(TAG, "outputblock created: " + " " + cell.getTitle() + " " + cell.getUUID().toString() + " " + " ");
		} catch (Exception e){
			Log.i(TAG, "outputblock exception: " + e.getMessage());
		}
	}
	
	// The output_block field of the JSON message
	protected String name;  

	private static String htmlify(String str) {
        Log.i(TAG,"Converting to HTML: "+str);
		StringBuilder s = new StringBuilder();
		s.append("<pre style=\"font-size:130%\">");
		String[] lines = str.split("\n");
		for (int i=0; i<lines.length; i++) {
			if (i>0)
				s.append("&#13;&#10;");
			s.append(TextUtils.htmlEncode(lines[i]));
		}
		s.append("</pre>");
		return s.toString();
	}
	
	public String getHtml() {
		StringBuilder s = new StringBuilder();
		s.append("<html><body>");
		ListIterator<String> iter = divs.listIterator();
		while (iter.hasNext()) {
			s.append("<div>");
			s.append(iter.next());
			s.append("</div>");
		}
		s.append("</body></html>");
		return s.toString();
	}
	
	private void addDiv(CommandOutput output) {

		Log.i(TAG, "Adding output: " + output.toLongString());
		
		if (output instanceof DataFile) 
			addDivDataFile((DataFile) output);
		else if (output instanceof HtmlFiles) 
			addDivHtmlFiles((HtmlFiles) output);
		else if (output instanceof DisplayData) 
			addDivDisplayData((DisplayData) output);
		else if (output instanceof PythonInput) {
			//addDivPythonInput((PythonInput) output);
		}
		else if (output instanceof PythonOutput) 
			addDivPythonOutput((PythonOutput) output);
		else if (output instanceof ResultStream) 
			addDivResultStream((ResultStream) output);
		else if (output instanceof Traceback) 
			addDivTraceback((Traceback) output);
		else if (output instanceof ExecuteReply) 
			addDivExecuteReply((ExecuteReply) output);
		else 
			divs.add("Unknown output: "+output.toShortString());
	}

	private void addDivDataFile(DataFile dataFile) {
		String uri = dataFile.getURI().toString();
		String div;
		String mime = dataFile.getMime();
		if (dataFile.mime().equals("image/png") || dataFile.mime().equals("image/jpg"))
			div = "<img src=\"" + uri + "\" alt=\"plot output\"></img>";
		else if (dataFile.mime().equals("image/svg"))
			div = "<object data\"" + uri + "\" type=\"image/svg+xml\"></object>";
		else
			div = "Unknow MIME type "+dataFile.mime();
		divs.add(div);
	}
	
	private void addDivHtmlFiles(HtmlFiles htmlFiles) {
		String div = "HTML";
		divs.add(div);
	}

	private void addDivDisplayData(DisplayData displayData) {
		String div = displayData.toHTML();
		Log.i(TAG, "addDivDisplayData");
		divs.add(div);
	}

	private void addDivPythonOutput(PythonOutput pythonOutput) {
		Log.i(TAG, "addDivPythonOutput");
		String div = htmlify(pythonOutput.get());
		divs.add(div);
	}
	
	
	private void addDivResultStream(ResultStream resultStream) {
		Log.i(TAG, "addDivResultStream");
		String div = htmlify(resultStream.get());
		divs.add(div);
	}

	private void addDivTraceback(Traceback traceback) {
		Log.i(TAG, "addDivTraceback");
		String div = htmlify(traceback.toString());
		divs.add(div);
	}
	
	private void addDivExecuteReply(ExecuteReply reply) {
		Log.i(TAG, "addDivExecuteReply");
		if (reply.getStatus().equals("ok"))
			divs.add("<font color=\"green\">ok</font>");
		else
			divs.add(reply.toString());
	}

	public void add(CommandOutput output) {
		Log.i(TAG, "add(CommandOutput output)" + output.toString());
		if (output.toString().contains("sys._sage_.update_interact")) {
			clearBlocks();
		}
		if (name == null) {
			Log.e(TAG, "adding output without initially setting it");
			return;
		}
		if (!name.equals(output.outputBlock()))
			Log.e(TAG, "Output has wrong output_block field");
		
		addDiv(output);
		// loadData(getHtml(), "text/html", "UTF-8");
		cell.saveOutput(getOutputBlock(), getHtml());
		loadUrl(cell.getUrlString(getOutputBlock()));
	}

	public void set(String output_block) {
		Log.i(TAG, "set(String output_block");
 		if (cell.hasCachedOutput(output_block))
			loadUrl(cell.getUrlString(output_block));
	}
	
	public void set(CommandOutput output) {
		Log.i(TAG, "set(CommandOutput output)" + output.toString());
		if (name == null) {
			name = output.outputBlock();
		}
		if (!name.equals(output.outputBlock()))
			Log.e(TAG, "Output has wrong output_block field");
		divs.clear();
		add(output);
	}
	
	public void clearBlocks() {
		divs.clear();
	}
	
	public void numberDivs() {
		for (String div:divs){
			Log.i(TAG, "EXISTING DIV: " + div);
		}
	}
	
	public String getOutputBlock() {
		return name;
	}
	
	public void setHTML(String html) {
		clearBlocks();
		divs.add(html);
	}
	
	public void setHistoryHTML() {
		loadUrl(cell.getUrlString(cell.getUUID().toString()));
	}
	
	public String getHTML() {
		String htmldata = "";
		for(String div: divs) {
			htmldata += div;
		}
		return htmldata;
	}
	
}
