package org.sagemath.droid.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import org.sagemath.droid.models.database.Cell;

/**
 * <p>Base {@linkplain android.support.v4.app.Fragment} for
 * {@linkplain org.sagemath.droid.fragments.CodeEditorFragment} &
 * {@linkplain org.sagemath.droid.fragments.OutputViewFragment}</p>
 *
 * @author Nikhil Peter Raj
 */
public class BaseFragment extends Fragment {
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
