package org.sagemath.droid.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import org.sagemath.droid.R;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.models.database.Inserts;
import org.sagemath.droid.utils.BusProvider;

import java.util.ArrayList;

/**
 * @author Nikhil Peter Raj
 */
public class DeleteInsertDialogFragment extends DialogFragment {
    private static final String TAG = "SageDroid:DeleteInsertDialogFragment";

    private static final String ARG_INSERTS = "inserts";

    //TODO refactor so all Delete dialogs extend base interface
    public interface OnDeleteListener {
        public void onDelete();
    }

    private OnDeleteListener listener;

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.listener = listener;
    }

    private ArrayList<Inserts> inserts;

    public static DeleteInsertDialogFragment newInstance(ArrayList<Inserts> inserts) {
        DeleteInsertDialogFragment fragment = new DeleteInsertDialogFragment();
        Bundle args = new Bundle();

        args.putParcelableArrayList(ARG_INSERTS, inserts);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BusProvider.getInstance().register(this);
        inserts = getArguments().getParcelableArrayList(ARG_INSERTS);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_delete_insert_title);
        builder.setMessage(getResources().getQuantityString(R.plurals.delete_n_inserts, inserts.size(), inserts.size())
                + "\n\n" + getString(R.string.delete_warning));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SageSQLiteOpenHelper.getInstance(getActivity()).deleteInsert(inserts);
                listener.onDelete();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        BusProvider.getInstance().unregister(this);
    }
}
