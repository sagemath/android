package org.sagemath.droid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import org.sagemath.droid.activities.SageActivity;
import org.sagemath.droid.adapters.CellListAdapter;
import org.sagemath.droid.cells.CellCollection;
import org.sagemath.droid.cells.CellData;
import org.sagemath.droid.dialogs.EditCellDialogFragment;

import java.util.LinkedList;


/**
 * CellListFragment - fragment containing list of cells in current group
 * shown in CellActivity (tablets) or CellListActivity (phones)
 *
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 */
public class CellListFragment
        extends ListFragment {
    private static final String TAG = "CellListFragment";


    protected LinkedList<CellData> cells = new LinkedList<CellData>();

    protected CellListAdapter adapter;

    @Override
    public void onResume() {
        super.onResume();
        switchToGroup(null);
        adapter.updateCellList(cells);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new CellListAdapter(getActivity(), cells);
        setListAdapter(adapter);
    }

    private static final String DIALOG_EDIT_CELL = "editCell";
    private CellData longClickedCell;

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
                mEditCellDialogFragment.setOnGroupSwitchedListener(new EditCellDialogFragment.onGroupSwitchListener() {
                    @Override
                    public void onGroupSwitched(String group) {
                        switchToGroup(group);
                        adapter.updateCellList(cells);

                    }
                });
                mEditCellDialogFragment.show(fm, DIALOG_EDIT_CELL);
                CellCollection.getInstance().saveCells();
                return true;
            }
        });
    }


    public void switchToGroup(String group) {
        CellCollection cellCollection = CellCollection.getInstance();
        cells.clear();
        if (group == null)
            group = cellCollection.getCurrentGroupName();
        cells.addAll(cellCollection.getGroup(group));
        if (cells.size() > 0) {
            cellCollection.setCurrentCell(cells.getFirst());
            getActivity().setTitle(cells.getFirst().getGroup());
        } else
            getActivity().getSupportFragmentManager().popBackStack();
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        CellData cell = cells.get(position);
        CellCollection.getInstance().setCurrentCell(cell);
        Intent i = new Intent(getActivity().getApplicationContext(), SageActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

}
