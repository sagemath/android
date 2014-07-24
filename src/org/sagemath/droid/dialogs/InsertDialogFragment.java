package org.sagemath.droid.dialogs;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import org.sagemath.droid.R;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.models.database.Inserts;
import org.sagemath.droid.utils.AnimationHelper;

/**
 * Created by Haven on 19-07-2014.
 */
public class InsertDialogFragment extends DialogFragment {
    private static final String TAG = "SageDroid:NewInsertDialogFragment";

    private static final String ARG_INSERT = "insert";

    public interface OnInsertCreateListener {
        public void onInsertCreated();
    }

    private OnInsertCreateListener listener;

    public void setOnInsertCreateListener(OnInsertCreateListener listener) {
        this.listener = listener;
    }

    private View dialogView;
    private Inserts insert;
    private EditText insertText, insertDescription;
    private CheckBox favoriteCheck;

    public static InsertDialogFragment newInstance(Inserts insert) {
        InsertDialogFragment fragment = new InsertDialogFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_INSERT, insert);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        insert = getArguments().getParcelable(ARG_INSERT);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_insert, null);
        builder.setView(dialogView);

        insertText = (EditText) dialogView.findViewById(R.id.insertText);
        insertDescription = (EditText) dialogView.findViewById(R.id.insertDesc);
        favoriteCheck = (CheckBox) dialogView.findViewById(R.id.insertFavoriteCheck);

        if (insert != null) {
            builder.setTitle(R.string.dialog_edit_insert_title);
            insertText.setText(insert.getInsertText());
            insertDescription.setText(insert.getInsertDescription());
            favoriteCheck.setChecked(insert.isFavorite());
        } else {
            builder.setTitle(R.string.dialog_new_insert_title);
        }

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        return builder.create();
    }

    private boolean areFieldsEmpty() {
        if (TextUtils.isEmpty(insertText.getText()) || TextUtils.isEmpty(insertDescription.getText())) {
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();

        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onClick(View v) {
                    if (areFieldsEmpty()) {
                        if (AnimationHelper.isIcsOrAbove()) {
                            AnimationHelper.Nope(dialogView).start();
                        } else {
                            AnimationHelper.SupportNope(dialogView).start();
                        }
                    } else {
                        if (insert == null) {
                            insert = new Inserts();
                        } else {
                            insert = new Inserts(insert.getId());
                        }
                        insert.setFavorite(favoriteCheck.isChecked());
                        insert.setInsertText(insertText.getText().toString());
                        insert.setInsertDescription(insertDescription.getText().toString());
                        SageSQLiteOpenHelper.getInstance(getActivity()).addInsert(insert);
                        if (listener != null) {
                            listener.onInsertCreated();
                        }
                        dismiss();
                    }
                }
            });
        }
    }
}
