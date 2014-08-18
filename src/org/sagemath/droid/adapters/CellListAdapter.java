package org.sagemath.droid.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import org.sagemath.droid.R;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.models.database.Group;
import org.sagemath.droid.utils.Highlighter;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter which is responsible for displaying the cells associated with each group.
 *
 * @author Rasmi.Elasmar
 * @author Nikhil Peter Raj
 */
public class CellListAdapter extends BaseAdapter implements StickyListHeadersAdapter {
    private static final String TAG = "SageDroid:CellListAdapter";
    private final Context context;

    private Typeface fontAwesome;
    private LayoutInflater inflater;
    private SparseBooleanArray checkedItems;

    private Drawable backgroundDrawable;
    private Highlighter highlighter;

    private List<Cell> cells;

    private SageSQLiteOpenHelper helper;
    private Group group;

    private String searchQuery = null;

    public CellListAdapter(Context context, Group group) {
        this.context = context;
        this.group = group;
        checkedItems = new SparseBooleanArray();
        inflater = LayoutInflater.from(context);
        highlighter = new Highlighter(context);
        helper = SageSQLiteOpenHelper.getInstance(context);
        refreshAdapter(group);
        fontAwesome = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
    }

    private class ViewHolder {
        public TextView titleView;
        public TextView descriptionView;
        public Button favorite;

        public void initFavorite() {
            favorite.setTypeface(fontAwesome);
            favorite.setTextColor(context.getResources().getColor(R.color.holo_green_light));
        }
    }

    private class HeaderViewHolder {
        public TextView headerView;
    }

    public void refreshAdapter(Group group) {
        updateCellList(helper.getCellsWithGroup(group));
    }

    public void updateCellList(List<Cell> cells) {
        Log.i(TAG, "Updating List with size: " + cells.size());
        this.cells = cells;
        notifyDataSetChanged();
    }

    public void setQueryCells(List<Cell> queryCells, String query) {
        cells = queryCells;
        searchQuery = query;
        notifyDataSetChanged();
    }

    public void queryReset(List<Cell> cells) {
        this.cells = cells;
        searchQuery = null;
        notifyDataSetChanged();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.list_header, viewGroup, false);
            holder.headerView = (TextView) convertView.findViewById(R.id.header_text);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        if (cells.get(position).isFavorite()) {
            holder.headerView.setText(convertView.getResources().getString(R.string.header_fav_text));
            return convertView;
        } else {
            Cell cell = cells.get(position);
            String title = cell.getTitle().toUpperCase().charAt(0) + "";
            holder.headerView.setText(title);
            return convertView;
        }
    }

    @Override
    public long getHeaderId(int position) {
        //All favorites return the same ID
        if (cells.get(position).isFavorite()) {
            return 1L;
        } else {
            return Character.toUpperCase(cells.get(position).getTitle().charAt(0));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = inflater.inflate(R.layout.item_cell_list, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.titleView = (TextView) view.findViewById(R.id.cellTitle);
            viewHolder.descriptionView = (TextView) view.findViewById(R.id.cellDescription);
            viewHolder.favorite = (Button) view.findViewById(R.id.cellFavorite);
            viewHolder.initFavorite();
            view.setTag(viewHolder);
            if (backgroundDrawable == null) {
                backgroundDrawable = view.getBackground();
            }
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        final Cell cell = cells.get(position);

        viewHolder.titleView.setText(highlighter.highlight(cell.getTitle(), searchQuery));
        viewHolder.descriptionView.setText(cell.getDescription());
        viewHolder.favorite.setText(cell.isFavorite() ? context.getString(R.string.fa_star) : context.getString(R.string.fa_star_outline));

        viewHolder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cell.setFavorite(!cell.isFavorite());
                helper.saveEditedCell(cell);
                refreshAdapter(group);
            }
        });

        if (checkedItems.get(position)) {
            view.setBackgroundDrawable(view.getResources().getDrawable(R.drawable.cell_selected_background));
        } else {
            view.setBackgroundDrawable(backgroundDrawable);
        }

        return view;
    }

    @Override
    public int getCount() {
        return cells.size();
    }

    @Override
    public Object getItem(int position) {
        return cells.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setSelection(int position, boolean value) {
        checkedItems.append(position, value);
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        int selected = 0;
        SparseBooleanArray checkedItems = getSelectedItems();
        for (int i = 0; i < checkedItems.size(); i++) {
            if (checkedItems.get(i)) {
                selected++;
            }
        }

        return selected;
    }

    public ArrayList<Cell> getSelectedItemList() {
        ArrayList<Cell> selectedItems = new ArrayList<>();
        for (int i = 0; i < checkedItems.size(); i++) {
            if (checkedItems.valueAt(i)) {
                selectedItems.add(cells.get(checkedItems.keyAt(i)));
            }
        }
        return selectedItems;
    }

    private SparseBooleanArray getSelectedItems() {
        return checkedItems;
    }

    public void clearSelection() {
        Log.i(TAG, "Clearing Selection");
        checkedItems.clear();
        notifyDataSetChanged();
    }

}

