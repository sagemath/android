package org.sagemath.droid.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import org.sagemath.droid.models.database.Cell;

/**
 * Created by Haven on 08-07-2014.
 */
public class BaseFragment extends Fragment {
    protected Bitmap expandIcon, collapseIcon;
    protected Cell cell;
    protected Typeface fontAwesome;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        fontAwesome = Typeface.createFromAsset(getActivity().getAssets(), "fontawesome-webfont.ttf");
    }

    protected void setCell(Cell cell) {
        this.cell = cell;
    }

}
