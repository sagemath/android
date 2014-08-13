package org.sagemath.droid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import org.sagemath.droid.R;
import org.sagemath.droid.adapters.HelpPagerAdapter;
import org.sagemath.droid.fragments.ChangelogFragment;
import org.sagemath.droid.fragments.HelpAboutFragment;
import org.sagemath.droid.fragments.HelpHtmlFragment;

/**
 * The Activity which hosts the Help Fragments
 *
 * @author Nikhil Peter Raj
 */
public class HelpActivity extends ActionBarActivity {
    private static final String TAG = "SageDroid:HelpActivity";

    public static final String EXTRA_SELECTED_TAB = "selectedTab";

    private static final int TAB_START = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Log.i(TAG, "In HelpActivity");

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        PagerTabStrip tabStrip = (PagerTabStrip) findViewById(R.id.pagerTabStrip);

        int selectedTab = TAB_START;
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(EXTRA_SELECTED_TAB)) {
            selectedTab = intent.getExtras().getInt(EXTRA_SELECTED_TAB);
        }

        HelpPagerAdapter tabAdapter = new HelpPagerAdapter(this);
        viewPager.setAdapter(tabAdapter);

        Bundle faqBundle = new Bundle();
        faqBundle.putInt(HelpHtmlFragment.ARG_HTML_FILE, R.raw.help_faq);
        tabAdapter.addTab(HelpHtmlFragment.class, faqBundle, getString(R.string.help_faq_title));

        tabAdapter.addTab(ChangelogFragment.class, null, getString(R.string.help_changelog_title));

        tabAdapter.addTab(HelpAboutFragment.class, null, getString(R.string.help_about_title));

        viewPager.setCurrentItem(selectedTab);
    }

}
