package org.sagemath.droid.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import org.sagemath.droid.R;
import org.sagemath.droid.models.database.Cell;

import java.util.ArrayList;

/**
 * <p>The {@link android.support.v4.app.DialogFragment} used to delete a cell</p>
 *
 * @author Nikhil Peter Raj
 */
public class DeleteCellDialogFragment extends BaseDeleteDialogFragment {

    private static final String TAG = "SageDroid:DeleteCellDialogFragment";

    private static final String ARG_CELL = "cell";

    public static DeleteCellDialogFragment newInstance(ArrayList<Cell> cells) {

        DeleteCellDialogFragment fragment = new DeleteCellDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_CELL, cells);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final ArrayList<Cell> cells = getArguments().getParcelableArrayList(ARG_CELL);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(getResources().getDrawable(R.drawable.ic_alert_red));
        builder.setTitle(getString(R.string.delete_dialog_title));
        builder.setMessage(getResources().getQuantityString(R.plurals.delete_n_cells, cells.size(), cells.size())
                + "\n\n" + getString(R.string.delete_warning));
        builder.setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDelete();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        return builder.create();
    }
}
