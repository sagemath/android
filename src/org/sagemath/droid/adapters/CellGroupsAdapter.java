package org.sagemath.droid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.sagemath.droid.R;
import org.sagemath.droid.models.database.Group;

import java.util.List;

/**
 * CellGroupsAdapter
 *
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 * @author Nikhil Peter Raj
 */
public class CellGroupsAdapter extends BaseAdapter {
    private final Context context;

    private List<Group> groups;

    private LayoutInflater inflater;

    public CellGroupsAdapter(Context context, List<Group> groups) {
        this.context = context;
        this.groups = groups;
        inflater = LayoutInflater.from(context);
    }

    public void refreshAdapter(List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public Object getItem(int position) {
        return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView item;
        if (convertView == null) {
            item = (TextView) inflater.inflate(R.layout.item_cell_group, parent, false);
        } else {
            item = (TextView) convertView;
        }
        item.setText(groups.get(position).getCellGroup());
        return item;
    }

}

