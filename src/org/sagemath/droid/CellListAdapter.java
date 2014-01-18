package org.sagemath.droid;

import java.util.LinkedList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * @author Rasmi.Elasmar
 *
 */
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
		protected CheckBox favorite;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View item;
		TextView titleView;
		TextView descriptionView;
		CheckBox favorite;
		final CellData cell = cells.get(position);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			item = inflater.inflate(R.layout.cell_list_item, parent, false);
			titleView = (TextView) item.findViewById(R.id.cell_title);
			descriptionView = (TextView) item.findViewById(R.id.cell_description);
			favorite = (CheckBox) item.findViewById(R.id.favorite);
			favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					cell.favorite = isChecked;
				}
			} );
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.titleView = titleView;
			viewHolder.descriptionView = descriptionView;
			viewHolder.favorite = favorite;
			item.setTag(viewHolder);
		} else {
			item = convertView;
			ViewHolder viewHolder = (ViewHolder)convertView.getTag();
			titleView = viewHolder.titleView;
			descriptionView = viewHolder.descriptionView;
			favorite = viewHolder.favorite;
		}

		titleView.setText(cell.title);
		descriptionView.setText(cell.description);
		favorite.setChecked(cell.isFavorite());

		return item;
	}

}

