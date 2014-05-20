package org.sagemath.droid.fragments;

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
import org.sagemath.droid.R;
import org.sagemath.droid.cells.CellCollection;
import org.sagemath.droid.cells.CellData;
import org.sagemath.droid.adapters.CellListAdapter;
import org.sagemath.droid.activities.SageActivity;


/**
 * CellListFragment - fragment containing list of cells in current group
 *                    shown in CellActivity (tablets) or CellListActivity (phones)
 * 
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 *
 */
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
	        			private EditText titleView, descView, groupView;
	        			
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
	        				groupView = (EditText)dialogView.findViewById(R.id.insert_cell_group);
	        				groupView.setText(longClickedCell.getGroup(), TextView.BufferType.EDITABLE);
	
	        				return new AlertDialog.Builder(getActivity())
	        				.setView(dialogView)
	        				.setTitle(R.string.edit_title)
	        				.setPositiveButton(android.R.string.ok, 
	        						new DialogInterface.OnClickListener() {
	        							@Override
	        							public void onClick(DialogInterface dialog, int which) {
	        								if (titleView.getText().toString().equals("")) {
	        									Date date = new Date();
	        									DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm aaa",Locale.US);
	        									longClickedCell.setTitle(dateFormat.format(date));
	        								} else {
	        									longClickedCell.setTitle(titleView.getText().toString());
	        								}
	        								longClickedCell.setDescription(descView.getText().toString());
	        								if (groupView.getText().toString().equals("")) {
	        									longClickedCell.setGroup("My Worksheets");
	        								} else {
	        									longClickedCell.setGroup(groupView.getText().toString());
	        								}
	        				    			CellCollection.getInstance().saveCells();
	        								switchToGroup(longClickedCell.getGroup());
	        				    			adapter = new CellListAdapter(getActivity(), cells);
	        				    			setListAdapter(adapter);
	        							}
	        						})
	        				.setNegativeButton(android.R.string.cancel, null)
	        				.create();
	        			}
	        		};
	    			dialog.show(fm, DIALOG_EDIT_CELL);
	    			CellCollection.getInstance().saveCells();
	                return true;
	            }
	    });
    }
    		
	

	
	public void switchToGroup(String group) {
		CellCollection cellCollection = CellCollection.getInstance();
		cells.clear();
		if (group == null)
			group = cellCollection.getCurrentGroupName();
		cells.addAll(cellCollection.getGroup(group));
		if (cells.size()>0) {
			cellCollection.setCurrentCell(cells.getFirst());
			getActivity().setTitle(cells.getFirst().getGroup());
		}
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
