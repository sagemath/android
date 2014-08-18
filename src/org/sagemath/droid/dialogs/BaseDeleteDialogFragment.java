package org.sagemath.droid.dialogs;

import android.support.v4.app.DialogFragment;

/**
 * <p>Base {@link android.support.v4.app.DialogFragment} which defines the interface to detect deletion</p>
 *
 * @author Nikhil Peter Raj
 */
public class BaseDeleteDialogFragment extends DialogFragment {

    public interface OnDeleteListener {
        public void onDelete();
    }

    protected OnDeleteListener listener;

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.listener = listener;
    }

}
