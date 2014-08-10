package org.sagemath.droid.dialogs;

import android.support.v4.app.DialogFragment;

/**
 * @author Nikhil Peter Raj
 */
public class BaseActionDialogFragment extends DialogFragment {

    public interface OnActionCompleteListener {
        public void onActionCompleted();
    }

    protected OnActionCompleteListener listener;

    public void setOnActionCompleteListener(OnActionCompleteListener listener) {
        this.listener = listener;
    }

}
