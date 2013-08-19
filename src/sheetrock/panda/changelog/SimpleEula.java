package sheetrock.panda.changelog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.sagemath.droid.R;
import org.sagemath.singlecellserver.SageSingleCell;
import org.sagemath.singlecellserver.SageSingleCell.SageInterruptedException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

public class SimpleEula {

	// Original SimpleEula via Donn Felker
	// https://github.com/donnfelker
	// Modified by Rasmi Elasmar for use in
	// the Sage Android Application

	private final static String TAG = "SimpleEula";
	private String EULA_PREFIX = "eula_";
	private boolean error;
	private Activity CellActivity;

	public SimpleEula(Activity context) {
		CellActivity = context; 
	}

	private PackageInfo getPackageInfo() {
		PackageInfo pi = null;
		try {
			pi = CellActivity.getPackageManager().getPackageInfo(CellActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi; 
	}

	public void show() throws ClientProtocolException, IOException, SageInterruptedException, JSONException, URISyntaxException {
		PackageInfo versionInfo = getPackageInfo();

		// the eulaKey changes every time you increment the version number in the AndroidManifest.xml
		final String eulaKey = EULA_PREFIX + versionInfo.versionCode;
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CellActivity);
		boolean hasBeenShown = prefs.getBoolean(eulaKey, false);
		if(hasBeenShown == false){

			// Show the Eula
			String title = CellActivity.getString(R.string.app_name) + " v" + versionInfo.versionName;

			//Includes the updates as well so users know what changed. 
			//String message = CellActivity.getString(R.string.updates) + "\n\n" + CellActivity.getString(R.string.eula);

			URI absolute = new URI("http://sagecell.sagemath.org");
			//URI(String scheme, String userInfo, String host, int port, String path, String query, String fragment)
			URI tosRelative = new URI("/tos.html");
			URI tosURI = absolute.resolve(tosRelative);
			int port = 10080;
			URI termsURI = new URI(tosURI.getScheme(), tosURI.getUserInfo(), tosURI.getHost(), port, 
					tosURI.getPath(), tosURI.getQuery(), tosURI.getFragment());
			Log.i(TAG, "Terms URI: " + termsURI.toString());
			HttpGet termsGet = new HttpGet();
			termsGet.setURI(tosURI);

			DefaultHttpClient termsHttpClient = new DefaultHttpClient();
			HttpResponse termsResponse = termsHttpClient.execute(termsGet);
			InputStream termsStream = termsResponse.getEntity().getContent();

			String termsHTML = SageSingleCell.streamToString(termsStream);
			Log.i(TAG, "Terms: " + termsHTML);
			String warning = "<p>By continuing, you indicate that you have read and agree to the <a href=\"http://sagecell.sagemath.org:10080/tos.html\">Sage Cell Server Terms of Usage</a>:</p>";
			
			if (termsHTML.contains("404") || termsHTML.contains("405")){
				error = true;
			}
			
			if (!error){
				AlertDialog dialog = new AlertDialog.Builder(CellActivity)
				.setTitle(title)
				.setMessage(Html.fromHtml((warning + termsHTML)))
				.setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						// Mark this version as read.
						SharedPreferences.Editor editor = prefs.edit();
						editor.putBoolean(eulaKey, true);
						editor.commit();
						dialogInterface.dismiss();
					}
				})
				.setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Close the activity as they have declined the EULA
						CellActivity.finish(); 
					}
				}).create();
				dialog.show();
				// Make links clickable, via Michael Burton: http://about.me/michaelburton
				((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
			}
		}
	}

}