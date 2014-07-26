package org.sagemath.droid.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.sagemath.droid.R;
import org.sufficientlysecure.htmltextview.HtmlTextView;

/**
 * @author Nikhil Peter Raj
 */
public class HelpAboutFragment extends Fragment {
    private static final String TAG = "SageDroid:HelpAboutFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_about, container, false);

        TextView versionText = (TextView) view.findViewById(R.id.helpAboutVersion);
        TextView gitIconText = (TextView) view.findViewById(R.id.gitIconText);
        HtmlTextView gitLinkText = (HtmlTextView) view.findViewById(R.id.gitLinkText);
        HtmlTextView helpAboutText = (HtmlTextView) view.findViewById(R.id.helpAboutText);

        versionText.setText(getVersion());

        gitIconText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fontawesome-webfont.ttf"));
        gitIconText.setText(R.string.fa_github);

        gitLinkText.setHtmlFromString(getString(R.string.git_link_text), false);

        helpAboutText.setHtmlFromRawResource(getActivity(), R.raw.help_about, false);

        return view;
    }

    private String getVersion() {
        String result = "";
        try {
            PackageManager manager = getActivity().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), 0);

            result = String.format("%s (%s)", info.versionName, info.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to get application version: " + e.getMessage());
            result = "Unable to get application version.";
        }

        return result;
    }
}
