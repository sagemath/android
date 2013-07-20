package org.sagemath.droid;

import java.util.LinkedList;
import java.util.ListIterator;

import org.sagemath.singlecellserver.CommandOutput;
import org.sagemath.singlecellserver.DataFile;
import org.sagemath.singlecellserver.DisplayData;
import org.sagemath.singlecellserver.ExecuteReply;
import org.sagemath.singlecellserver.HtmlFiles;
import org.sagemath.singlecellserver.PythonInput;
import org.sagemath.singlecellserver.PythonOutput;
import org.sagemath.singlecellserver.ResultStream;
import org.sagemath.singlecellserver.Traceback;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

public class OutputBlock extends WebView {
	private final static String TAG = "OutputBlock";

	private final CellData cell;
	
	public OutputBlock(Context context, CellData cell) {
		super(context);
		this.cell = cell;
	}
	
	// The output_block field of the JSON message
	protected String name;  

	private static String htmlify(String str) {
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

	private LinkedList<String> divs = new LinkedList<String>();
	
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
		if (output instanceof DataFile) 
			addDivDataFile((DataFile) output);
		else if (output instanceof HtmlFiles) 
			addDivHtmlFiles((HtmlFiles) output);
		else if (output instanceof DisplayData) 
			addDivDisplayData((DisplayData) output);
		else if (output instanceof PythonInput) 
			addDivPythonInput((PythonInput) output);
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
		divs.add(div);
	}

	private void addDivPythonOutput(PythonOutput pythonOutput) {
		String div = htmlify(pythonOutput.get());
		divs.add(div);
	}
	
	private void addDivPythonInput(PythonInput pythonInput) {
		String div = htmlify(pythonInput.get());
		divs.add(div);
	}
	
	private void addDivResultStream(ResultStream resultStream) {
		String div = htmlify(resultStream.get());
		divs.add(div);
	}

	private void addDivTraceback(Traceback traceback) {
		String div = htmlify(traceback.toString());
		divs.add(div);
	}
	
	private void addDivExecuteReply(ExecuteReply reply) {
		if (reply.getStatus().equals("ok"))
			divs.add("<font color=\"green\">ok</font>");
		else
			divs.add(reply.toString());
	}

	public void add(CommandOutput output) {
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
 		if (cell.hasCachedOutput(output_block))
			loadUrl(cell.getUrlString(output_block));
	}
	
	public void set(CommandOutput output) {
		if (name == null) {
			name = output.outputBlock();
		}
		if (!name.equals(output.outputBlock()))
			Log.e(TAG, "Output has wrong output_block field");
		divs.clear();
		add(output);
	}
	
	public String getOutputBlock() {
		return name;
	}
	
	
}
