package org.sagemath.droid.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import org.sagemath.droid.R;
import org.sagemath.droid.cells.CellCollection;
import org.sagemath.droid.cells.CellData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by Haven on 21-05-2014.
 */
public class EditCellDialogFragment extends DialogFragment {

    private static final String TAG = "SageDroid:EditCellDialogFragment";
    private static final String ARG_CELL = "Cell";

    public interface onGroupSwitchListener{
        public void onGroupSwitched(String group);
    }

    private EditText titleView;
    private EditText descView;
    private AutoCompleteTextView groupView;

    private LinkedList<String> cellGroups;
    private ArrayAdapter<String> adapter;
    private String[] groupChoices;

    private CellData selectedCell;

    private onGroupSwitchListener mListener;

    public void setOnGroupSwitchedListener(onGroupSwitchListener listener){
        mListener=listener;
    }

    public static EditCellDialogFragment newInstance(CellData cell) {

        EditCellDialogFragment frag = new EditCellDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CELL, cell);

        frag.setArguments(args);

        return frag;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        selectedCell = getArguments().getParcelable(ARG_CELL);

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit, null);

        titleView = (EditText) dialogView.findViewById(R.id.insert_cell_title);
        titleView.setText(selectedCell.getTitle(), TextView.BufferType.EDITABLE);
        descView = (EditText) dialogView.findViewById(R.id.insert_cell_desc);
        descView.setText(selectedCell.getDescription(), TextView.BufferType.EDITABLE);
        groupView = (AutoCompleteTextView) dialogView.findViewById(R.id.insert_cell_group);
        groupView.setText(selectedCell.getGroup(), TextView.BufferType.EDITABLE);

        cellGroups= CellCollection.getInstance().groups();
        groupChoices = new String[cellGroups.size()];
        cellGroups.toArray(groupChoices);
        adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,groupChoices);
        groupView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        builder.setTitle(R.string.edit_title);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (titleView.getText().toString().equals("")) {
                    Date date = new Date();
                    DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm aaa", Locale.US);
                    selectedCell.setTitle(dateFormat.format(date));
                } else {
                    selectedCell.setTitle(titleView.getText().toString());
                }
                selectedCell.setDescription(descView.getText().toString());
                if (groupView.getText().toString().equals("")) {
                    selectedCell.updateGroup("My Worksheets");
                } else {
                    selectedCell.updateGroup(groupView.getText().toString());
                }
                CellCollection.getInstance().saveCells();
                mListener.onGroupSwitched(selectedCell.getGroup());
            }
        });
        builder.setNegativeButton(android.R.string.cancel,null);
        return builder.create();
    }
}
