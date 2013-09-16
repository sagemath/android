package org.sagemath.droid;

import java.util.LinkedList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

public class CellListFragment 
		extends ListFragment {
	private static final String TAG = "CellListFragment";
	
	
	protected LinkedList<CellData> cells = new LinkedList<CellData>();
	
	protected CellListAdapter adapter;
	
	@Override
	public void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		adapter = new CellListAdapter(getActivity(), cells);
		setListAdapter(adapter);
	}
	
	public void switchToGroup(String group) {
		CellCollection cellCollection = CellCollection.getInstance();
		cells.clear();
		if (group == null)
			cells.addAll(cellCollection.getCurrentGroup());
		else
			cells.addAll(cellCollection.getGroup(group));		
		cellCollection.setCurrentCell(cells.getFirst());
		if (adapter != null)
			adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//CheckBox favorite = (CheckBox) l.findViewById(R.id.favorite);
		
		CellData cell = cells.get(position);
		CellCollection.getInstance().setCurrentCell(cell);
		Intent i = new Intent(getActivity().getApplicationContext(), SageActivity.class);
		startActivity(i);
	}

}
