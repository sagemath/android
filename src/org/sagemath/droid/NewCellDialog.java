package org.sagemath.droid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 *
 */
public class NewCellDialog extends DialogFragment {
	
	private EditText title;
	private EditText group;
	private EditText input;
	
	
	@SuppressLint("NewApi")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View dialogView = getActivity().getLayoutInflater()
				.inflate(R.layout.dialog_new, null);
		final Context mContext = dialogView.getContext();
		
		title = (EditText)dialogView.findViewById(R.id.insert_cell_title);
		group = (EditText)dialogView.findViewById(R.id.insert_cell_group);
		input = (EditText)dialogView.findViewById(R.id.insert_cell_input);
		
		String g = CellCollection.getInstance().getCurrentGroupName();
		if (g.equals("History"))
			g = "";
		group.setText(g);		
		
		final AlertDialog d = new AlertDialog.Builder(getActivity())
        .setView(dialogView)
        .setTitle(R.string.add_new_title)
        .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
        .setNegativeButton(android.R.string.cancel, null)
        .create();

		d.setOnShowListener(new DialogInterface.OnShowListener() {

    @Override
    public void onShow(DialogInterface dialog) {

        final Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                
            	CellData newCell = new CellData();

				if (title.getText().toString().equals("")) {
					Date date = new Date();
					DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm aaa",Locale.US);							
					newCell.title = dateFormat.format(date);
				} else {
					newCell.title = title.getText().toString();
				}
				if (group.getText().toString().equals("")) {
					newCell.group = "My Worksheets";
				} else {
					newCell.group = group.getText().toString();
				}
				
				if (input.getText().toString().equals("")) {
					
					Toast.makeText(mContext, "Enter an input to calculate!", Toast.LENGTH_SHORT).show();
					return;
				} else {
					newCell.input = input.getText().toString();
					
				}
				
				newCell.rank = 0;
				CellCollection.getInstance().addCell(newCell);
				CellCollection.getInstance().setCurrentCell(newCell);
			
				
			Intent i = new Intent(getActivity().getApplicationContext(),
						SageActivity.class);
				i.putExtra("NEWCELL", true);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				
            	
            	
                //Dismiss once everything is OK.
                d.dismiss();
            }
        });
    }
});
				

return d ;
	}
}
