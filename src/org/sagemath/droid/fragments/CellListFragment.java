package org.sagemath.droid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import org.sagemath.droid.R;
import org.sagemath.droid.activities.SageActivity;
import org.sagemath.droid.adapters.CellListAdapter;
import org.sagemath.droid.constants.StringConstants;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.dialogs.EditCellDialogFragment;
import org.sagemath.droid.models.database.Cell;

import java.util.ArrayList;
import java.util.List;


/**
 * CellListFragment - fragment containing list of cells in current group
 * shown in CellActivity (tablets) or CellListActivity (phones)
 *
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 */
public class CellListFragment
        extends ListFragment {
    private static final String TAG = "SageDroid:CellListFragment";

    private static final String DIALOG_EDIT_CELL = "editCell";
    private static final String ARG_GROUP = "group";
    private Cell longClickedCell;


    private List<Cell> cells = new ArrayList<Cell>();
    private SageSQLiteOpenHelper helper;
    private String group;

    private CellListAdapter adapter;

    public CellListFragment() {
        helper = SageSQLiteOpenHelper.getInstance(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (group != null && adapter != null) {
            Log.i(TAG, "Updating Cells with group:" + group);
            cells = helper.getCellsWithGroup(group);
            adapter.updateCellList(cells);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_GROUP, group);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "In onCreate");
        super.onCreate(savedInstanceState);
        if ((savedInstanceState != null) && (savedInstanceState.getString(ARG_GROUP) != null)) {
            this.group = savedInstanceState.getString(ARG_GROUP);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_cell_list, null);

    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int pos, long id) {
                longClickedCell = cells.get(pos);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                EditCellDialogFragment mEditCellDialogFragment = EditCellDialogFragment.newInstance(longClickedCell);
                mEditCellDialogFragment.setOnCellEditListener(new EditCellDialogFragment.OnCellEditListener() {
                    @Override
                    public void onCellEdited() {
                        refreshAdapter();
                    }
                });
                mEditCellDialogFragment.show(fm, DIALOG_EDIT_CELL);
                return true;
            }
        });

    }

    public void refreshAdapter() {
        if (group != null)
            adapter.updateCellList(helper.getCellsWithGroup(group));
    }

    public void setGroup(String group) {
        this.group = group;
        getActivity().setTitle(group);
        cells = helper.getCellsWithGroup(group);
        adapter = new CellListAdapter(getActivity(), cells);
        setListAdapter(adapter);
    }

    public void switchToGroup(String group) {
        cells.clear();
        if (group == null)
            group = helper.getGroups().get(0);
        cells.addAll(helper.getCellsWithGroup(group));
        if (cells.size() > 0) {
            getActivity().setTitle(cells.get(0).getTitle());
        } else
            getActivity().getSupportFragmentManager().popBackStack();
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Cell cell = cells.get(position);
        Intent i = new Intent(getActivity().getApplicationContext(), SageActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(StringConstants.ID, cell.getID());
        startActivity(i);
    }
}
