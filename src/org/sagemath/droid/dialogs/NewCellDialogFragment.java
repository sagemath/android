package org.sagemath.droid.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import org.sagemath.droid.R;
import org.sagemath.droid.activities.SageActivity;
import org.sagemath.droid.constants.StringConstants;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.models.database.Cell;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 */
public class NewCellDialogFragment extends DialogFragment {

    public interface OnCellCreateListener {
        public void onCellCreated();
    }

    private OnCellCreateListener listener;

    public void setOnCellCreateListener(OnCellCreateListener listener) {
        this.listener = listener;
    }

    private static final String TAG = "SageDroid:NewCellDialogFragment";

    private EditText title;
    private AutoCompleteTextView group;
    private EditText input;
    private boolean isInputEmpty;

    private List<String> cellGroups;
    private ArrayAdapter<String> adapter;
    private String[] groupChoices;

    public static NewCellDialogFragment newInstance() {
        NewCellDialogFragment frag = new NewCellDialogFragment();

        return frag;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_new, null);

        title = (EditText) dialogView.findViewById(R.id.insert_cell_title);
        group = (AutoCompleteTextView) dialogView.findViewById(R.id.insert_cell_group);
        input = (EditText) dialogView.findViewById(R.id.insert_cell_input);

        final SageSQLiteOpenHelper helper = SageSQLiteOpenHelper.getInstance(getActivity());

        cellGroups = helper.getGroups();
        groupChoices = new String[cellGroups.size()];
        cellGroups.toArray(groupChoices);
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, groupChoices);
        group.setAdapter(adapter);

        return new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setTitle(R.string.add_new_title)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Cell cell = new Cell();

                                if (title.getText().toString().equals("")) {
                                    Date date = new Date();
                                    DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm aaa", Locale.US);
                                    cell.setTitle(dateFormat.format(date));
                                } else {
                                    String currentTitle = title.getText().toString();
                                    cell.setTitle(currentTitle);
                                }
                                if (group.getText().toString().equals("")) {
                                    cell.setGroup("My Worksheets");
                                } else {
                                    String currentGroup = group.getText().toString();
                                    cell.setGroup(currentGroup);
                                }
                                if (input.getText().toString().equals("")) {
                                    isInputEmpty = true;
                                    cell.setInput("");
                                } else {
                                    isInputEmpty = false;
                                    String currentInput = input.getText().toString();
                                    cell.setInput(currentInput);
                                }

                                cell.setRank(0);
                                Long id = helper.addCell(cell);

                                if (id != null) {
                                    listener.onCellCreated();
                                }

                                Log.i(TAG, "Added cell " + cell.toString());

                                Intent intent = new Intent(getActivity().getApplicationContext(),
                                        SageActivity.class);
                                intent.putExtra("NEWCELL", true);
                                intent.putExtra(StringConstants.ID, id);
                                intent.putExtra(StringConstants.FLAG_INPUT_EMPTY, isInputEmpty);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);

                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
