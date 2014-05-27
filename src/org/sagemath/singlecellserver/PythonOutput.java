package org.sagemath.singlecellserver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


/**
 * <h1>Python output</h1>
 * <p>
 * When Python produces output from code that has been compiled in with the
 * 'single' flag to :func:`compile`, any expression that produces a value (such as
 * ``1+1``) is passed to ``sys.displayhook``, which is a callable that can do with
 * this value whatever it wants.  The default behavior of ``sys.displayhook`` in
 * the Python interactive prompt is to print to ``sys.stdout`` the :func:`repr` of
 * the value as long as it is not ``None`` (which isn't printed at all).  In our
 * case, the kernel instantiates as ``sys.displayhook`` an object which has
 * similar behavior, but which instead of printing to stdout, broadcasts these
 * values as ``pyout`` messages for clients to display appropriately.
 * <p>
 * IPython's displayhook can handle multiple simultaneous formats depending on its
 * configuration. The default pretty-printed repr text is always given with the
 * ``data`` entry in this message. Any other formats are provided in the
 * ``extra_formats`` list. Frontends are free to display any or all of these
 * according to its capabilities. ``extra_formats`` list contains 3-tuples of an ID
 * string, a type string, and the data. The ID is unique to the formatter
 * implementation that created the data. Frontends will typically ignore the ID
 * unless if it has requested a particular formatter. The type string tells the
 * frontend how to interpret the data. It is often, but not always a MIME type.
 * Frontends should ignore types that it does not understand. The data itself is
 * any JSON object and depends on the format. It is often, but not always a string.
 * <p>
 * Message type: ``pyout``::
 * <p>
 * <pre><code>
 *   content = {
 *        # The counter for this execution is also provided so that clients can
 *        # display it, since IPython automatically creates variables called _N
 *        # (for prompt N).
 *        'execution_count' : int,
 *        
 *        # The data dict contains key/value pairs, where the kids are MIME
 *        # types and the values are the raw data of the representation in that
 *        # format. The data dict must minimally contain the ``text/plain``
 *        # MIME type which is used as a backup representation.
 *        'data' : dict,
 *    }
 * </code></pre>
 * 
 * @author vbraun
 */
public class PythonOutput extends CommandOutput {
	private final static String TAG = "SageDroid:PythonOutput";
	
	protected JSONObject content, data;
	protected String text;
	
	protected PythonOutput(JSONObject json) throws JSONException {
		super(json);
		//Log.i(TAG, "PythonOutput created!");
		content = json.getJSONObject("content");
		data = content.getJSONObject("data");
		text = data.getString("text/plain");
	}

	public String toString() {
		return "Python output: "+text;
	}
	
	public String toShortString() {
		return "Python output";
	}

	/**
	 * Get an iterator for the possible encodings.
	 * 
	 * @return
	 */
	public Iterator<?> getEncodings() {
		return data.keys();
	}
	
	/**
	 * Get the output
	 * 
	 * @param encoding Which of possibly multiple representations to return
	 * @return The output in the chosen representation
	 * @throws JSONException
	 */
	public String get(String encoding) throws JSONException {
		return data.getString(encoding);
	}
	
	/**
	 * Return a textual representation of the output
	 * 
	 * @return Text representation of the output;
	 */
	public String get() {
		return text;
	}
	
}
