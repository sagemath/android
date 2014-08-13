package org.sagemath.droid.utils;

import android.content.Context;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import org.sagemath.droid.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util to highlight a given span depending on it's query
 *
 * @author Nikhil Peter Raj
 */
public class Highlighter {

    private Context context;

    public Highlighter(Context context) {
        this.context = context;
    }

    public Spannable highlight(String text, String searchQuery) {
        Spannable highlight = Spannable.Factory.getInstance().newSpannable(text);

        if (searchQuery == null) {
            return highlight;
        }

        Pattern pattern = Pattern.compile("(?i)(" + searchQuery.trim().replaceAll("\\s+", "|") + ")");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            highlight.setSpan(
                    new ForegroundColorSpan(context.getResources().getColor(R.color.holo_blue_light)),
                    matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return highlight;
    }
}
