package org.sagemath.droid.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import org.sagemath.droid.R;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.models.database.Group;

/**
 * @author Nikhil Peter Raj
 */
public class DeleteGroupDialogFragment extends DialogFragment {
    private static final String TAG = "SageDroid:DeleteGroupDialogFragment";

    private static final String ARG_GROUP = "group";

    private Group group;

    public interface OnGroupDeleteListener {
        public void onGroupDeleted();
    }

    private OnGroupDeleteListener listener;

    public void setOnGroupDeleteListener(OnGroupDeleteListener listener) {
        this.listener = listener;
    }

    public static DeleteGroupDialogFragment newInstance(Group group) {
        DeleteGroupDialogFragment fragment = new DeleteGroupDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_GROUP, group);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        group = getArguments().getParcelable(ARG_GROUP);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_alert_red);
        builder.setTitle(R.string.group_delete_title);
        builder.setMessage(R.string.delete_group_warning);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SageSQLiteOpenHelper.getInstance(getActivity()).deleteGroup(group);
                if (listener != null) {
                    listener.onGroupDeleted();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }
}
