package org.sagemath.droid.fragments;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import com.github.johnpersano.supertoasts.SuperToast;
import org.sagemath.droid.R;
import org.sagemath.droid.adapters.InsertsAdapter;
import org.sagemath.droid.dialogs.DeleteInsertDialogFragment;
import org.sagemath.droid.dialogs.InsertDialogFragment;
import org.sagemath.droid.models.database.Insert;
import org.sagemath.droid.utils.ToastUtils;

import java.util.ArrayList;

/**
 * ListFragment which displays the Insert
 *
 * @author Nikhil Peter Raj
 */
public class ManageInsertFragment extends ListFragment implements SearchView.OnQueryTextListener {
    private static final String TAG = "SageDroid:ManageInsertFragment";

    private static final String ARG_EDIT_INSERT = "editInsert";
    private static final String ARG_NEW_INSERT = "newInsert";
    private static final String ARG_DELETE_INSERT = "deleteInsert";

    private InsertsAdapter adapter;
    private Drawable enableEditIcon, disableEditIcon;
    private boolean isEditEnabled = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_insert, container);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new InsertsAdapter(getActivity(), true);
        setListAdapter(adapter);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
            getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    mode.setTitle(getListView().getCheckedItemCount() + "");
                    mode.invalidate();
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    if (enableEditIcon == null) {
                        enableEditIcon = getResources().getDrawable(R.drawable.ic_action_content_edit_enabled);
                        disableEditIcon = getResources().getDrawable(R.drawable.ic_action_content_edit);
                    }

                    MenuItem editItem = menu.findItem(R.id.menu_action_edit);

                    if (getListView().getCheckedItemCount() == 1) {
                        editItem.setIcon(enableEditIcon);
                        isEditEnabled = true;
                    } else {
                        editItem.setIcon(disableEditIcon);
                        isEditEnabled = false;
                    }

                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_action_toggle_fav:
                            adapter.toggleSelection(getListView().getCheckedItemPositions());
                            mode.finish();
                            break;

                        case R.id.menu_action_edit:
                            if (isEditEnabled) {
                                ArrayList<Insert> editSelection = adapter.getSelectedInserts(getListView().getCheckedItemPositions());
                                Insert insert = editSelection.get(0);
                                FragmentManager fm = getActivity().getSupportFragmentManager();
                                InsertDialogFragment dialog = InsertDialogFragment.newInstance(insert);
                                dialog.show(fm, ARG_EDIT_INSERT);
                            } else {
                                ToastUtils.getAlertToast(getActivity()
                                        , R.string.toast_edit_multiple_error
                                        , SuperToast.Duration.SHORT).show();
                            }
                            mode.finish();
                            break;

                        case R.id.menu_action_delete:
                            ArrayList<Insert> deleteSelection = adapter.getSelectedInserts(getListView().getCheckedItemPositions());
                            DeleteInsertDialogFragment dialog = DeleteInsertDialogFragment.newInstance(deleteSelection);
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            dialog.show(fm, ARG_DELETE_INSERT);
                            dialog.setOnDeleteListener(new DeleteInsertDialogFragment.OnDeleteListener() {
                                @Override
                                public void onDelete() {
                                    adapter.refreshAdapter();
                                }
                            });
                            mode.finish();
                            break;
                    }
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    mode = null;
                }
            });
        } else {
            getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            registerForContextMenu(getListView());
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_cell_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        ArrayList<Insert> items = new ArrayList<>();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        Insert insert;

        switch (item.getItemId()) {
            case R.id.menu_toggle_fav:
                adapter.toggleSelection(info.position);
                break;

            case R.id.menu_edit:
                insert = (Insert) adapter.getItem(info.position);
                InsertDialogFragment dialog = InsertDialogFragment.newInstance(insert);
                dialog.show(fm, ARG_EDIT_INSERT);
                break;

            case R.id.menu_delete:
                insert = (Insert) adapter.getItem(info.position);
                items.add(insert);
                DeleteInsertDialogFragment deleteDialog = DeleteInsertDialogFragment.newInstance(items);
                deleteDialog.show(fm, ARG_DELETE_INSERT);
                break;

        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        adapter.queryInsert(s);
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_manage_inserts, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_search_inserts);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.insert_query_hint));
        searchView.setOnQueryTextListener(this);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    MenuItemCompat.collapseActionView(searchItem);
                    searchView.setQuery("", false);
                }
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                adapter.refreshAdapter();
                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_insert:
                InsertDialogFragment dialog = InsertDialogFragment.newInstance(null);
                dialog.show(getActivity().getSupportFragmentManager(), ARG_NEW_INSERT);
                dialog.setOnInsertCreateListener(new InsertDialogFragment.OnInsertCreateListener() {
                    @Override
                    public void onInsertCreated() {
                        adapter.refreshAdapter();
                    }
                });
                break;


            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
