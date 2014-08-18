package org.sagemath.droid.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import org.sagemath.droid.R;
import org.sagemath.droid.adapters.InsertsAdapter;
import org.sagemath.droid.models.database.Insert;

/**
 * <p>The {@link android.support.v4.app.DialogFragment} which displays the insert selection dialog in {@link org.sagemath.droid.activities.SageActivity}</p>
 *
 * @author Nikhil Peter Raj
 */
public class InsertSpinnerDialogFragment extends DialogFragment {
    private static final String TAG = "SageDroid:InsertSpinnerDialogFragment";

    public interface OnInsertSelectedListener {
        public void onInsertSelected(Insert insert);
    }

    private OnInsertSelectedListener listener;

    public void setOnInsertSelectedListener(OnInsertSelectedListener listener) {
        this.listener = listener;
    }

    public static InsertSpinnerDialogFragment newInstance() {
        return new InsertSpinnerDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final InsertsAdapter adapter = new InsertsAdapter(getActivity(), false);

        builder.setTitle(R.string.dialog_insert_title);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onInsertSelected((Insert) adapter.getItem(which));
                }
                dismiss();
            }
        });
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();

    }
}
