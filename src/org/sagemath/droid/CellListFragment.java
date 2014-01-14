package org.sagemath.droid;

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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

public class CellListFragment 
		extends ListFragment {
	private static final String TAG = "CellListFragment";
	
	
	protected LinkedList<CellData> cells = new LinkedList<CellData>();
	
	protected CellListAdapter adapter;
	
	@Override
	public void onResume() {
		super.onResume();
		switchToGroup(null);
		adapter = new CellListAdapter(getActivity(), cells);
		setListAdapter(adapter);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		adapter = new CellListAdapter(getActivity(), cells);
		setListAdapter(adapter);
	}

	private static final String DIALOG_EDIT_CELL = "editCell";
	private CellData longClickedCell;

	@Override
	public void onActivityCreated(Bundle savedState) {
	    super.onActivityCreated(savedState);

	    getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
	            @Override
	            public boolean onItemLongClick(AdapterView<?> parent, View view,
	                    int pos, long id) {
	        		longClickedCell = cells.get(pos);
	        	    FragmentManager fm = getActivity().getSupportFragmentManager();
	        		DialogFragment dialog = new DialogFragment() {
	        			private EditText titleView;
	        			private EditText descView;
	        			
	        			@Override
	        			public View onCreateView (LayoutInflater inflater,
	        					ViewGroup container, Bundle savedInstanceState) {
	        				return null;
	        			}
	        			
	        			@Override
	        			public Dialog onCreateDialog(Bundle savedInstanceState) {
	        				View dialogView = getActivity().getLayoutInflater()
	        						.inflate(R.layout.dialog_edit, null);
	        				
	        				titleView = (EditText)dialogView.findViewById(R.id.insert_cell_title);
	        				titleView.setText(longClickedCell.getTitle(), TextView.BufferType.EDITABLE);
	        				descView = (EditText)dialogView.findViewById(R.id.insert_cell_desc);
	        				descView.setText(longClickedCell.getDescription(), TextView.BufferType.EDITABLE);
	        				
	        				return new AlertDialog.Builder(getActivity())
	        				.setView(dialogView)
	        				.setTitle(R.string.add_new_title)
	        				.setPositiveButton(android.R.string.ok, 
	        						new DialogInterface.OnClickListener() {
	        							@Override
	        							public void onClick(DialogInterface dialog, int which) {
	        								String titleStr, descStr;
	        								if (titleView.getText().toString().equals("")) {
	        									Date date = new Date();
	        									DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm aaa",Locale.US);
	        									longClickedCell.setTitle(dateFormat.format(date));
	        								} else {
	        									longClickedCell.setTitle(titleView.getText().toString());
	        								}
	        								longClickedCell.setDescription(descView.getText().toString());
	        				    			adapter = new CellListAdapter(getActivity(), cells);
	        				    			setListAdapter(adapter);
	        							}
	        						})
	        				.setNegativeButton(android.R.string.cancel, null)
	        				.create();
	        			}
	        		};
	    			dialog.show(fm, DIALOG_EDIT_CELL);
	                return true;
	            }
	    });
    }
    		
	

	
	public void switchToGroup(String group) {
		CellCollection cellCollection = CellCollection.getInstance();
		cells.clear();
		if (group == null) {
			LinkedList<CellData> data = cellCollection.getCurrentGroup();
			if (data != null)
				cells.addAll(data);
			else
				return;
		}
		else
			cells.addAll(cellCollection.getGroup(group));
		if (cells.size()>0)
			cellCollection.setCurrentCell(cells.getFirst());
		else
			getActivity().getSupportFragmentManager().popBackStack();
		if (adapter != null)
			adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		CellData cell = cells.get(position);
		CellCollection.getInstance().setCurrentCell(cell);
		Intent i = new Intent(getActivity().getApplicationContext(), SageActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}

}
