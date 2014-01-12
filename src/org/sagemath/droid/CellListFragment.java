package org.sagemath.droid;

import java.util.LinkedList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

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
		startActivity(i);
	}

}
