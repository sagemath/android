package org.sagemath.droid.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import org.sagemath.droid.R;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.models.database.Cell;

import java.util.Collections;
import java.util.List;

/**
 * @author Rasmi.Elasmar
 */
public class CellListAdapter extends ArrayAdapter<Cell> {
    private static final String TAG = "SageDroid:CellListAdapter";
    private final Context context;

    private List<Cell> cells;

    public CellListAdapter(Context context, List<Cell> cells) {
        super(context, R.layout.cell_list_item, cells);
        this.context = context;
        this.cells = cells;
    }

    static class ViewHolder {
        protected TextView titleView;
        protected TextView descriptionView;
        protected CheckBox favorite;
    }

    public void updateCellList(List<Cell> cells) {
        Log.i(TAG, "Updating List: " + cells.toString());
        this.cells = cells;
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
                    Cell cell = cells.get(my_position);
                    cell.setFavorite(!cell.isFavorite());
                    cells = SageSQLiteOpenHelper
                            .getInstance(getContext())
                            .getCellsWithGroup(cell.getGroup());
                    Collections.sort(cells, new Cell.CellComparator());
                    notifyDataSetChanged();
                }
            });
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.titleView = titleView;
            viewHolder.descriptionView = descriptionView;
            viewHolder.favorite = favorite;
            item.setTag(viewHolder);
        } else {
            item = convertView;
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            titleView = viewHolder.titleView;
            descriptionView = viewHolder.descriptionView;
            favorite = viewHolder.favorite;
        }

        Cell cell = cells.get(my_position);
        titleView.setText(cell.getTitle());
        descriptionView.setText(cell.getDescription());
        favorite.setChecked(cell.isFavorite());

        return item;
    }

}

