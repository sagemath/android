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
import android.widget.TextView;
import org.sagemath.droid.R;
import org.sagemath.droid.models.database.Cell;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rasmi.Elasmar
 */
public class CellListAdapter extends BaseAdapter implements StickyListHeadersAdapter {
    private static final String TAG = "SageDroid:CellListAdapter";
    private final Context context;

    private Typeface fontAwesome;
    private LayoutInflater inflater;
    private SparseBooleanArray checkedItems;

    private boolean clearSelection = false;

    private Drawable backgroundDrawable;

    private List<Cell> cells;

    public CellListAdapter(Context context, List<Cell> cells) {
        this.context = context;
        this.cells = cells;
        checkedItems = new SparseBooleanArray();
        inflater = LayoutInflater.from(context);
        fontAwesome = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
    }

    private class ViewHolder {
        public TextView titleView;
        public TextView descriptionView;
        public TextView favorite;

        public void initFavorite() {
            favorite.setTypeface(fontAwesome);
            favorite.setTextColor(context.getResources().getColor(R.color.holo_blue_light));
        }

    }

    private class HeaderViewHolder {
        public TextView headerView;
    }

    public void updateCellList(List<Cell> cells) {
        Log.i(TAG, "Updating List with size: " + cells.size());
        this.cells = cells;
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
            viewHolder.titleView = (TextView) view.findViewById(R.id.cell_title);
            viewHolder.descriptionView = (TextView) view.findViewById(R.id.cell_description);
            viewHolder.favorite = (TextView) view.findViewById(R.id.favorite);
            viewHolder.initFavorite();
            view.setTag(viewHolder);
            if (backgroundDrawable == null) {
                backgroundDrawable = view.getBackground();
            }
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        Cell cell = cells.get(position);

        viewHolder.titleView.setText(cell.getTitle());
        viewHolder.descriptionView.setText(cell.getDescription());
        viewHolder.favorite.setText(cell.isFavorite() ? context.getString(R.string.fa_star) : context.getString(R.string.fa_star_outline));

        if (clearSelection) {
            view.setBackgroundDrawable(backgroundDrawable);
        } else {
            if (checkedItems.get(position)) {
                Log.i(TAG, "Setting blue background");
                view.setBackgroundDrawable(view.getResources().getDrawable(R.drawable.cell_selected_background));
            } else {
                Log.i(TAG, "Setting normal background");
                view.setBackgroundDrawable(backgroundDrawable);
            }
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

    public void resetSelection() {
        clearSelection = false;
    }

    public void clearSelection() {
        Log.i(TAG, "Clearing Selection");
        checkedItems.clear();
        notifyDataSetChanged();
    }

}

