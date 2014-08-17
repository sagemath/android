package org.sagemath.droid.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.*;
import android.widget.*;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;
import org.sagemath.droid.R;
import org.sagemath.droid.activities.HelpActivity;
import org.sagemath.droid.activities.SageActivity;
import org.sagemath.droid.activities.SettingsActivity;
import org.sagemath.droid.adapters.CellGroupsAdapter;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.dialogs.BaseDeleteDialogFragment;
import org.sagemath.droid.dialogs.DeleteGroupDialogFragment;
import org.sagemath.droid.dialogs.GroupDialogFragment;
import org.sagemath.droid.models.database.Group;

import java.util.List;


/**
 * Fragment which displays the Cell Groups
 *
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 * @author Nikhil Peter Raj
 */
public class CellGroupsFragment extends ListFragment {
    private static final String TAG = "SageDroid:CellGroupsFragment";

    private static final String ARG_EDIT_GROUP_DIALOG = "groupDialog";
    private static final String ARG_DELETE_GROUP_DIALOG = "deleteGroupDialog";
    private static final String DIALOG_NEW_GROUP = "newGroup";

    public static final String KEY_GROUP_PLAYGROUND = "playgroundGroup";

    private SageSQLiteOpenHelper helper;

    private TextView playgroundItem;
    private ImageButton playgroundInfoButton;

    public interface OnGroupSelectedListener {
        public void onGroupSelected(Group group);
    }

    private OnGroupSelectedListener listener;

    public void setOnGroupSelected(OnGroupSelectedListener listener) {
        this.listener = listener;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        Group group = groups.get(position);
        if (isLandscape()) {
            //In Landscape, highlight the current group and also select it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                getListView().setItemChecked(position, true);
                getListView().performClick();
            }
        }
        listener.onGroupSelected(group);
    }

    protected List<Group> groups;

    protected CellGroupsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = SageSQLiteOpenHelper.getInstance(getActivity());
        groups = helper.getGroups();
        adapter = new CellGroupsAdapter(getActivity().getApplicationContext(), groups);
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        groups = helper.getGroups();
        adapter.refreshAdapter(groups);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_cell_group, container, false);
        playgroundItem = (TextView) view.findViewById(R.id.playgroundItem);
        playgroundInfoButton = (ImageButton) view.findViewById(R.id.playgroundInfo);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        if (!isLandscape())
            setHasOptionsMenu(true);
        registerForContextMenu(getListView());
        playgroundItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SageActivity.class);
                intent.putExtra(KEY_GROUP_PLAYGROUND, true);
                startActivity(intent);
            }
        });
        playgroundInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlaygroundInfoToast();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.menu_activity_cell, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add: {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                GroupDialogFragment dialog = GroupDialogFragment.newInstance(null);
                dialog.setOnActionCompleteListener(new GroupDialogFragment.OnActionCompleteListener() {
                    @Override
                    public void onActionCompleted() {
                        groups = helper.getGroups();
                        adapter.refreshAdapter(groups);
                    }
                });
                dialog.show(fm, DIALOG_NEW_GROUP);
                return true;
            }
            case R.id.menu_help:
                startActivity(new Intent(getActivity(), HelpActivity.class));
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.menu_group_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        Group group = (Group) adapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.menu_group_edit:
                GroupDialogFragment dialog = GroupDialogFragment.newInstance(group);
                dialog.show(fm, ARG_EDIT_GROUP_DIALOG);
                return true;

            case R.id.menu_group_delete:
                DeleteGroupDialogFragment deleteDialog = DeleteGroupDialogFragment.newInstance(group);
                deleteDialog.setOnDeleteListener(new BaseDeleteDialogFragment.OnDeleteListener() {
                    @Override
                    public void onDelete() {
                        groups = helper.getGroups();
                        adapter.refreshAdapter(groups);
                    }
                });
                deleteDialog.show(fm, ARG_DELETE_GROUP_DIALOG);
                return true;

            default:
                return super.onContextItemSelected(item);
        }

    }

    //For external access
    public void updateGroups() {
        groups = helper.getGroups();
        adapter.refreshAdapter(groups);
    }

    private void showPlaygroundInfoToast() {
        SuperCardToast toast = new SuperCardToast(getActivity());
        toast.setText(getString(R.string.toast_playground_info));
        toast.setIcon(R.drawable.ic_action_about, SuperToast.IconPosition.LEFT);
        toast.setBackground(SuperToast.Background.GREEN);
        toast.setTextColor(Color.BLACK);
        toast.setDuration(3000);
        toast.show();
    }

    private boolean isLandscape() {
        return Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation;
    }
}
