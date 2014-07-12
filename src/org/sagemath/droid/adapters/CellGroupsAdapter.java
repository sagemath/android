package org.sagemath.droid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.sagemath.droid.R;

import java.util.List;

/**
 * CellGroupsAdapter
 *
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 */
public class CellGroupsAdapter extends ArrayAdapter<String> {
    private final Context context;

    private List<String> groups;

    public CellGroupsAdapter(Context context, List<String> groups) {
        super(context, R.layout.item_cell_group, groups);
        this.context = context;
        this.groups = groups;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView item;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            item = (TextView) inflater.inflate(R.layout.item_cell_group, parent, false);
        } else {
            item = (TextView) convertView;
        }
        item.setText(groups.get(position));
        return item;
    }

}

