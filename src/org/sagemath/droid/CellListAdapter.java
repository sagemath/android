package org.sagemath.droid;

import java.util.LinkedList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CellListAdapter extends ArrayAdapter<CellData>  {
	private final Context context;
	
	private LinkedList<CellData> cells;
	
	public CellListAdapter(Context context, LinkedList<CellData> cells) {
		super(context, R.layout.cell_list_item, cells);
		this.context = context;
		this.cells = cells;
	}
	
	static class ViewHolder {
		protected TextView titleView;
		protected TextView descriptionView;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View item;
		TextView titleView;
		TextView descriptionView;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			item = inflater.inflate(R.layout.cell_list_item, parent, false);
			titleView = (TextView) item.findViewById(R.id.cell_title);
			descriptionView = (TextView) item.findViewById(R.id.cell_description);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.titleView = titleView;
			viewHolder.descriptionView = descriptionView;
			item.setTag(viewHolder);
		} else {
			item = convertView;
			ViewHolder viewHolder = (ViewHolder)convertView.getTag();
			titleView = viewHolder.titleView;
			descriptionView = viewHolder.descriptionView;
		}
		CellData cell = cells.get(position);
		titleView.setText(cell.title);
		descriptionView.setText(cell.description);
		return item;
	}

}

