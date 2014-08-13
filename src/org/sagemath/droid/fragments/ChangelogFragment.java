package org.sagemath.droid.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.sagemath.droid.R;

/**
 * Fragment hosting the Changelog
 * @author Nikhil Peter Raj
 */
public class ChangelogFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_changelog, container, false);
    }
}
