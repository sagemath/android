package org.sagemath.droid.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBarActivity;

import java.util.ArrayList;

/**
 * @author Nikhil Peter Raj
 */
public class HelpPagerAdapter extends FragmentPagerAdapter {
    public static final String TAG = "SageDroid:HelpPagerAdapter";

    private static final int TAB_FAQ = 1;
    private static final int TAB_CHANGELOG = 2;
    private static final int TAB_ABOUT = 3;

    ActionBarActivity activity;
    ArrayList<TabInfo> tabs = new ArrayList<>();

    static final class TabInfo {
        public final Class<?> clss;
        public final Bundle args;
        public final String title;

        TabInfo(Class<?> clss, Bundle args, String title) {
            this.clss = clss;
            this.args = args;
            this.title = title;
        }
    }

    public HelpPagerAdapter(ActionBarActivity activity) {
        super(activity.getSupportFragmentManager());
        this.activity = activity;
    }

    public void addTab(Class<?> clss, Bundle args, String title) {
        TabInfo tabInfo = new TabInfo(clss, args, title);
        tabs.add(tabInfo);
        notifyDataSetChanged();
    }

    public void removeTab(int index) {
        tabs.remove(index);
        notifyDataSetChanged();
    }


    @Override
    public Fragment getItem(int position) {
        TabInfo info = tabs.get(position);
        return Fragment.instantiate(activity, info.clss.getName(), info.args);
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position).title;
    }
}
