package org.sagemath.droid.dialogs;

import android.support.v4.app.DialogFragment;

/**
 * <p>Base {@link android.support.v4.app.DialogFragment} which defines the interface to listen for action completion(add/delete) etc.</p>
 *
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
