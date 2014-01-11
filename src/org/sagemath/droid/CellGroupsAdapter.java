package org.sagemath.droid;

import java.util.LinkedList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CellGroupsAdapter extends ArrayAdapter<String>  {
	private final Context context;
	
	private LinkedList<String> groups;
	
	public CellGroupsAdapter(Context context, LinkedList<String> groups) {
		super(context, R.layout.cell_groups_item, groups);
		this.context = context;
		this.groups = groups;
		
		selected = groups.indexOf(CellCollection.getInstance().getCurrentGroupName());
	}
	
	private int selected;
	
	public void setSelectedItem(int position) {
		selected = position;
		notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView item;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			item = (TextView) inflater.inflate(R.layout.cell_groups_item, parent, false);
		} else {
			item = (TextView) convertView;
		}
		item.setText(groups.get(position));
		if (position == selected)
			item.setBackgroundResource(R.drawable.white);
		else
			item.setBackgroundResource(R.drawable.transparent);
		return item;
	}

}

