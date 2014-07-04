package org.sagemath.droid.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import org.sagemath.droid.R;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.models.database.Cell;

/**
 * Created by Haven on 30-06-2014.
 */
public class DeleteCellDialogFragment extends DialogFragment {

    public interface OnCellDeleteListener {
        public void onCellDeleted();
    }

    private OnCellDeleteListener listener;

    public void setOnCellDeleteListener(OnCellDeleteListener listener) {
        this.listener = listener;
    }

    private static final String TAG = "SageDroid:DeleteCellDialogFragment";

    private static final String ARG_CELL = "cell";

    public static DeleteCellDialogFragment newInstance(Cell cell) {

        DeleteCellDialogFragment fragment = new DeleteCellDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CELL, cell);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Cell cell = getArguments().getParcelable(ARG_CELL);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.dialog_confirm_discard);
        builder.setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SageSQLiteOpenHelper.getInstance(getActivity()).deleteCell(cell);
                listener.onCellDeleted();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        return builder.create();
    }
}
