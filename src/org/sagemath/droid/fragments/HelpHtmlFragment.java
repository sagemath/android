package org.sagemath.droid.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import org.sufficientlysecure.htmltextview.HtmlTextView;

/**
 * Fragment which displays an html file loaded from raw
 * @author Nikhil Peter Raj
 */
public class HelpHtmlFragment extends Fragment {

    public static final String ARG_HTML_FILE = "htmlFile";

    Activity activity;
    int htmlFile;


    static HelpHtmlFragment newInstance(int htmlFile) {
        HelpHtmlFragment f = new HelpHtmlFragment();

        // Supply html raw file input as an argument.
        Bundle args = new Bundle();
        args.putInt(ARG_HTML_FILE, htmlFile);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();

        htmlFile = getArguments().getInt(ARG_HTML_FILE);

        ScrollView scroller = new ScrollView(activity);
        HtmlTextView text = new HtmlTextView(activity);


        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, activity
                .getResources().getDisplayMetrics());
        text.setPadding(padding, padding, padding, 0);

        scroller.addView(text);


        text.setHtmlFromRawResource(getActivity(), htmlFile, true);


        text.setTextColor(getResources().getColor(android.R.color.black));

        return scroller;
    }
}
