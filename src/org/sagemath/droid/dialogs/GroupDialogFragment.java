package org.sagemath.droid.dialogs;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.sagemath.droid.R;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.models.database.Group;
import org.sagemath.droid.utils.AnimationHelper;

/**
 * @author Nikhil Peter Raj
 */
public class GroupDialogFragment extends BaseActionDialogFragment {
    private static final String TAG = "SageDroid:GroupDialogFragment";

    private static final String ARG_GROUP = "group";

    private Group group;

    private View dialogView;
    private EditText groupText;
    private SageSQLiteOpenHelper helper;

    public static GroupDialogFragment newInstance(Group group) {
        GroupDialogFragment fragment = new GroupDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_GROUP, group);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        group = getArguments().getParcelable(ARG_GROUP);

        dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_group, null);

        helper = SageSQLiteOpenHelper.getInstance(getActivity());

        groupText = (EditText) dialogView.findViewById(R.id.groupText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        if (group == null) {
            builder.setTitle(R.string.group_new_dialog_title);
        } else {
            builder.setTitle(R.string.group_edit_dialog_title);
        }
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        if (group != null) {
            groupText.setText(group.getCellGroup());
        }

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog d = (AlertDialog) getDialog();

        if (d != null) {
            Button postiveButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
            postiveButton.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(groupText.getText())) {
                        if (AnimationHelper.isIcsOrAbove()) {
                            AnimationHelper.Nope(dialogView).start();
                        } else {
                            AnimationHelper.SupportNope(dialogView).start();
                        }
                    } else {
                        if (group != null) {
                            group.setCellGroup(groupText.getText().toString());
                        } else {
                            group = new Group(groupText.getText().toString());
                        }
                        helper.saveGroup(group);
                        if (listener != null)
                            listener.onActionCompleted();
                        dismiss();
                    }
                }
            });
        }
    }
}
