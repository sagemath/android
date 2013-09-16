package org.sagemath.droid;

import java.util.LinkedList;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class CellGroupsFragment extends ListFragment {
	private static final String TAG = "CellGroupsFragment";

	interface OnGroupSelectedListener {
		public void onGroupSelected(String group);
	}
	
	private OnGroupSelectedListener listener;
	
	public void setOnGroupSelected(OnGroupSelectedListener listener) {
		this.listener = listener;
	}
	
	
	@Override
	public void onListItemClick(ListView parent, View view, int position, long id) {
		adapter.setSelectedItem(position);
		String group = groups.get(position);
		listener.onGroupSelected(group);
	}
		
	protected LinkedList<String> groups;
	
	protected CellGroupsAdapter adapter;
	
	@Override
	public void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		groups = CellCollection.getInstance().groups();
		adapter = new CellGroupsAdapter(getActivity().getApplicationContext(), groups);
		setListAdapter(adapter);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.cell_groups_layout, container);
	}
	
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    Window window = activity.getWindow();
	    window.setFormat(PixelFormat.RGBA_8888);
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		CellCollection.getInstance().saveCells();
	}
	
}
