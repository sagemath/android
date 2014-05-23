package org.sagemath.droid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import org.sagemath.droid.R;
import org.sagemath.droid.cells.CellCollection;
import org.sagemath.droid.cells.CellData;

import java.util.LinkedList;

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

    public void updateCellList(LinkedList<CellData> cells)
    {
        this.cells=cells;
        notifyDataSetChanged();
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View item;
		TextView titleView;
		TextView descriptionView;
		final CheckBox favorite;
		final int my_position = position;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			item = inflater.inflate(R.layout.cell_list_item, parent, false);
			titleView = (TextView) item.findViewById(R.id.cell_title);
			descriptionView = (TextView) item.findViewById(R.id.cell_description);
			favorite = (CheckBox) item.findViewById(R.id.favorite);
			favorite.setOnClickListener(new CompoundButton.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					CellData cell = cells.get(my_position);
					cell.setFavorite(!cell.isFavorite());
					cells = CellCollection.getInstance().getGroup(cell.getGroup());
					notifyDataSetChanged();
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

		CellData cell = cells.get(my_position);
		titleView.setText(cell.getTitle());
		descriptionView.setText(cell.getDescription());
		favorite.setChecked(cell.isFavorite());

		return item;
	}

}

