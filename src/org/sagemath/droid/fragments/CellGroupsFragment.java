package org.sagemath.droid.fragments;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import org.sagemath.droid.R;
import org.sagemath.droid.adapters.CellGroupsAdapter;
import org.sagemath.droid.cells.CellCollection;
import org.sagemath.droid.database.SageSQLiteOpenHelper;

import java.util.List;


/**
 * CellGroupsFragment - fragment showing the group list
 *
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 */
public class CellGroupsFragment extends ListFragment {
    private static final String TAG = "SageDroid:CellGroupsFragment";

    public interface OnGroupSelectedListener {
        public void onGroupSelected(String group);
    }

    private OnGroupSelectedListener listener;

    public void setOnGroupSelected(OnGroupSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        String group = groups.get(position);
        listener.onGroupSelected(group);
    }

    protected List<String> groups;

    protected CellGroupsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groups = SageSQLiteOpenHelper.getInstance(getActivity()).getGroups();
        adapter = new CellGroupsAdapter(getActivity().getApplicationContext(), groups);
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        groups = SageSQLiteOpenHelper.getInstance(getActivity()).getGroups();
        adapter = new CellGroupsAdapter(getActivity().getApplicationContext(), groups);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cell_groups_layout, null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Window window = activity.getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

}
