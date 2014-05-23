package org.sagemath.droid.dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.*;
import org.sagemath.droid.R;
import org.sagemath.droid.cells.CellCollection;
import org.sagemath.droid.cells.CellData;
import org.sagemath.droid.activities.SageActivity;

/**
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 *
 */
public class NewCellDialogFragment extends DialogFragment {

    private static final String TAG="DialogFragment";

    private EditText title;
    private AutoCompleteTextView group;
    private EditText input;

    private LinkedList<String> cellGroups;
    private ArrayAdapter<String> adapter;
    private String[] groupChoices;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_new, null);

        title = (EditText)dialogView.findViewById(R.id.insert_cell_title);
        group = (AutoCompleteTextView)dialogView.findViewById(R.id.insert_cell_group);
        input = (EditText)dialogView.findViewById(R.id.insert_cell_input);

        cellGroups= CellCollection.getInstance().groups();
        groupChoices = new String[cellGroups.size()];
        cellGroups.toArray(groupChoices);
        adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,groupChoices);
        group.setAdapter(adapter);

        String currentGroup = CellCollection.getInstance().getCurrentGroupName();
        if (currentGroup.equals("History"))
            currentGroup="";
        group.setText(currentGroup);

        return new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setTitle(R.string.add_new_title)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CellData newCell = new CellData();

                                if (title.getText().toString().equals("")) {
                                    Date date = new Date();
                                    DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm aaa",Locale.US);
                                    newCell.setTitle(dateFormat.format(date));
                                } else {
                                    String currentTitle = title.getText().toString();
                                    newCell.setTitle(currentTitle);
                                }
                                if (group.getText().toString().equals("")) {
                                    newCell.setGroup("My Worksheets");
                                } else {
                                    String currentGroup= group.getText().toString();
                                    newCell.setGroup(currentGroup);
                                }
                                if (input.getText().toString().equals("")) {
                                    Toast.makeText(getActivity(), "Enter an input to calculate!", Toast.LENGTH_SHORT).show();
                                    return;
                                } else {
                                    String currentInput=input.getText().toString();
                                    newCell.setInput(currentInput);
                                }

                                newCell.setRank(0);
                                CellCollection.getInstance().addCell(newCell);
                                CellCollection.getInstance().setCurrentCell(newCell);


                                Intent i = new Intent(getActivity().getApplicationContext(),
                                        SageActivity.class);
                                i.putExtra("NEWCELL", true);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);

                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
