package org.sagemath.droid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class NewCellDialog extends DialogFragment {
	
	private EditText title;
	private EditText group;
	private EditText input;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View dialogView = getActivity().getLayoutInflater()
				.inflate(R.layout.dialog_new, null);
		
		title = (EditText)dialogView.findViewById(R.id.insert_cell_title);
		group = (EditText)dialogView.findViewById(R.id.insert_cell_group);
		input = (EditText)dialogView.findViewById(R.id.insert_cell_input);
		
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
							newCell.title = dateFormat.format(date);
						} else {
							newCell.title = title.getText().toString();
						}
						if (group.getText().toString().equals("")) {
							newCell.group = "Uncategorized";
						} else {
							newCell.group = group.getText().toString();
						}
						if (input.getText().toString().equals("")) {
							Toast.makeText(getActivity(), "Enter an input to calculate!", Toast.LENGTH_SHORT).show();
							return;
						} else {
							newCell.input = input.getText().toString();
						}
						
						newCell.rank = (int) Math.random()*30;
						CellCollection.getInstance().addCell(newCell);
						CellCollection.getInstance().setCurrentCell(newCell);
					
						
						Intent i = new Intent(getActivity().getApplicationContext(), SageActivity.class);
						i.putExtra("NEWCELL", true);
						startActivity(i);
						
					}
				})
		.setNegativeButton(android.R.string.cancel, null)
		.create();
	}
}
