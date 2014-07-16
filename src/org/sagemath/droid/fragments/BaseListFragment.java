package org.sagemath.droid.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.sagemath.droid.R;

/**
 * Created by Haven on 16-07-2014.
 */
public class BaseListFragment extends Fragment {
    private boolean contentShown;
    private ViewGroup container;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.list_layout, container, false);

        contentShown = true;
        this.container = (ViewGroup) root.findViewById(R.id.listview_container);
        contentShown = false;

        return root;
    }

    protected ViewGroup getContainer() {
        return container;
    }

    public void setContentShown(boolean shown, boolean animate) {
        if (contentShown == shown) {
            return;
        }
        contentShown = shown;
        if (shown) {
            container.setVisibility(View.VISIBLE);
        } else {
            container.setVisibility(View.INVISIBLE);
        }
    }

    public void setContentShown(boolean shown) {
        setContentShown(shown, true);
    }

    public void setContentShownNoAnimation(boolean shown) {
        setContentShown(shown, false);
    }


}
