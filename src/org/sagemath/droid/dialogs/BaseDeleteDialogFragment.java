package org.sagemath.droid.dialogs;

import android.support.v4.app.DialogFragment;

/**
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
