package org.sagemath.droid.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import org.sagemath.droid.R;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.models.database.Insert;
import org.sagemath.droid.utils.Highlighter;

import java.util.ArrayList;
import java.util.List;

/**
 * The Adapter responsible for displaying the Inserts.
 * @author Nikhil Peter Raj
 */
public class InsertsAdapter extends BaseAdapter {
    private static final String TAG = "SageDroid:InsertsAdapter";

    private Context context;
    private LayoutInflater inflater;
    private SageSQLiteOpenHelper helper;
    private Typeface fontAwesome;

    private Highlighter highlighter;

    private boolean fullDescription = false;
    private String searchQuery = null;

    private List<Insert> inserts;

    public InsertsAdapter(Context context, boolean fullDescription) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fullDescription = fullDescription;
        helper = SageSQLiteOpenHelper.getInstance(context);
        highlighter = new Highlighter(context);
        fontAwesome = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
        inserts = helper.getInserts();
    }

    private class ViewHolder {
        public TextView insertDescriptionText;
        public TextView insertText;
        public Button insertFavoriteButton;
    }

    public void refreshAdapter() {
        inserts = helper.getInserts();
        searchQuery = null;
        notifyDataSetChanged();
    }

    public void queryInsert(String query) {
        inserts = helper.getQueryInserts(query);
        searchQuery = query;
        notifyDataSetChanged();
    }

    public ArrayList<Insert> getSelectedInserts(SparseBooleanArray selection) {
        ArrayList<Insert> selectedInserts = new ArrayList<>();
        for (int i = 0; i < selection.size(); i++) {
            if (selection.valueAt(i)) {
                selectedInserts.add(inserts.get(selection.keyAt(i)));
            }
        }
        return selectedInserts;
    }

    public void toggleSelection(SparseBooleanArray selection) {
        List<Insert> toggleSelection = new ArrayList<>();
        for (int i = 0; i < selection.size(); i++) {
            if (selection.valueAt(i)) {
                Insert insert = inserts.get(selection.keyAt(i));
                insert.setFavorite(!insert.isFavorite());
                toggleSelection.add(insert);
            }
        }
        helper.addInsert(toggleSelection);
        refreshAdapter();
    }

    public void toggleSelection(int position) {
        Insert insert = inserts.get(position);
        insert.setFavorite(!insert.isFavorite());
        helper.addInsert(insert);
        refreshAdapter();
    }

    @Override
    public int getCount() {
        return inserts.size();
    }

    @Override
    public Object getItem(int position) {
        return inserts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        View view;
        if (convertView == null) {
            if (fullDescription) {
                view = inflater.inflate(R.layout.item_inserts_description, parent, false);
            } else {
                view = inflater.inflate(R.layout.item_inserts, parent, false);
            }
            viewHolder = new ViewHolder();
            if (fullDescription) {
                viewHolder.insertText = (TextView) view.findViewById(R.id.insertText);
            }
            viewHolder.insertDescriptionText = (TextView) view.findViewById(R.id.insertName);
            viewHolder.insertFavoriteButton = (Button) view.findViewById(R.id.insertFav);
            viewHolder.insertFavoriteButton.setTypeface(fontAwesome);
            viewHolder.insertFavoriteButton.setTextColor(view.getResources().getColor(R.color.holo_green_light));
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        final Insert insert = inserts.get(position);

        if (fullDescription) {
            viewHolder.insertText.setText(insert.getInsertText());
        }
        viewHolder.insertDescriptionText.setText(highlighter.highlight(insert.getInsertDescription(), searchQuery));
        viewHolder.insertFavoriteButton.setText(insert.isFavorite() ?
                context.getString(R.string.fa_star)
                : context.getString(R.string.fa_star_outline));

        viewHolder.insertFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insert.setFavorite(!insert.isFavorite());
                helper.addInsert(insert);
                refreshAdapter();
            }
        });
        return view;
    }


}
