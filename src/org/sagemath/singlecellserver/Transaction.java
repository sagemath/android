package org.sagemath.singlecellserver;

import java.util.LinkedList;
import java.util.ListIterator;


public class Transaction {
	protected final SageSingleCell server;
	protected final CommandRequest request;
	protected final LinkedList<CommandReply> reply;

	public static class Factory {
		public Transaction newTransaction(SageSingleCell server,
				CommandRequest request, LinkedList<CommandReply> reply) {
			return new Transaction(server, request, reply);  
		}
	}
	
	protected Transaction(SageSingleCell server, 
			CommandRequest request, LinkedList<CommandReply> reply) {
		this.server = server;
		this.request = request;
		this.reply = reply;
	}
	
	public DataFile getDataFile() {
		for (CommandReply r: reply) 
			if (r instanceof DataFile) return (DataFile)r;
		return null;
	}

	public HtmlFiles getHtmlFiles() {
		for (CommandReply r: reply) 
			if (r instanceof HtmlFiles) return (HtmlFiles)r;
		return null;
	}
	
	public DisplayData getDisplayData() {
		for (CommandReply r: reply) 
			if (r instanceof DisplayData) return (DisplayData)r;
		return null;
	}

	public ResultStream getResultStream() {
		for (CommandReply r: reply) 
			if (r instanceof ResultStream) return (ResultStream)r;
		return null;
	}
	
	public PythonOutput getPythonOutput() {
		for (CommandReply r: reply) 
			if (r instanceof PythonOutput) return (PythonOutput)r;
		return null;
	}
	
	public Traceback getTraceback() {
		for (CommandReply r: reply) 
			if (r instanceof Traceback) return (Traceback)r;
		return null;
	}
	
	public HttpError getHttpError() {
		for (CommandReply r: reply) 
			if (r instanceof HttpError) return (HttpError)r;
		return null;
	}

	public ExecuteReply getExecuteReply() {
		for (CommandReply r: reply) 
			if (r instanceof ExecuteReply) return (ExecuteReply)r;
		return null;
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(request);
		if (reply.isEmpty())
			s.append("no reply");
		else
			s.append("\n");
		ListIterator<CommandReply> iter = reply.listIterator();
		while (iter.hasNext()) {
			s.append(iter.next());
			if (iter.hasNext())
				s.append("\n");
		}
		return s.toString();
	}
	
}


