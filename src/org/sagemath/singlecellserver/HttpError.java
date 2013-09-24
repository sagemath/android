package org.sagemath.singlecellserver;

public class HttpError extends CommandReply {
	private final static String TAG = "HttpError"; 
	
	protected String message;
	
	protected HttpError(CommandRequest request, String message) {
		super(request);
		this.message = message;
	}
	
	public String toString() {
		return "HTTP error "+message;
	}
	
	@Override
	public boolean terminateServerConnection() {
		return true;
	}
	
}
