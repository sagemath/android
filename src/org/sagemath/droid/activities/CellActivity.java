package org.sagemath.droid.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import org.sagemath.droid.R;
import org.sagemath.droid.constants.IntConstants;
import org.sagemath.droid.dialogs.BaseActionDialogFragment;
import org.sagemath.droid.dialogs.CellDialogFragment;
import org.sagemath.droid.dialogs.GroupDialogFragment;
import org.sagemath.droid.fragments.CellGroupsFragment;
import org.sagemath.droid.fragments.CellGroupsFragment.OnGroupSelectedListener;
import org.sagemath.droid.fragments.CellListFragment;
import org.sagemath.droid.models.database.Group;
import org.sagemath.droid.utils.SimpleEula;

/**
 * CellActivity - Main Activity, First Screen
 *
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 * @author Nikhil Peter Raj
 */
public class CellActivity
        extends ActionBarActivity
        implements OnGroupSelectedListener,
        PopupMenu.OnMenuItemClickListener {
    private final static String TAG = "SageDroid:CellActivity";

    private static final String DIALOG_NEW_GROUP = "newGroup";
    private static final String DIALOG_NEW_CELL = "newCell";

    private CellGroupsFragment groupsFragment;
    private CellListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cell);
        try {
            new SimpleEula(this).new EulaTask().execute();
        } catch (Exception e) {
            Log.e(TAG, "Error showing EULA: " + e.toString());
            e.printStackTrace();
        }

        getSupportActionBar().setTitle(R.string.main_menu_title);

        groupsFragment = (CellGroupsFragment)
                getSupportFragmentManager().findFragmentById(R.id.cellGroupFragment);
        groupsFragment.setOnGroupSelected(this);

        listFragment = (CellListFragment)
                getSupportFragmentManager().findFragmentById(R.id.cellListFragment);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now that the UI is visible to user, if both fragments are visible and we are in Landscape,
        //select the first group as default
        if (groupsFragment != null && listFragment != null && isLandscape()) {
            View view = groupsFragment.getListAdapter().getView(0, null, null);
            long id = groupsFragment.getListAdapter().getItemId(0);
            //If getView() returns null, we have no groups, so don't perform the click
            if (view != null) {
                groupsFragment.getListView().performItemClick(view, 0, id);
            }

        }
    }

    public static final String INTENT_SWITCH_GROUP = "intent_switch_group";

    @Override
    public void onGroupSelected(Group group) {
        CellListFragment listFragment = (CellListFragment)
                getSupportFragmentManager().findFragmentById(R.id.cellListFragment);
        if (listFragment == null || !listFragment.isInLayout()) {
            //Start new Activity since we are in Phone Layout
            Intent i = new Intent(getApplicationContext(), CellListActivity.class);
            i.putExtra(INTENT_SWITCH_GROUP, group);
            startActivity(i);
        } else {
            listFragment.setGroup(group);
        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return isLandscape();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isLandscape()) {
            //Let activity handle the addition via Popup
            getMenuInflater().inflate(R.menu.menu_cell_landscape, menu);
            return true;
        } else {
            return false;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean superMenu = super.onOptionsItemSelected(item);
        if (isLandscape()) {
            switch (item.getItemId()) {

                case R.id.menu_add_popup:
                    View popUpView = findViewById(R.id.menu_add_popup);
                    showPopUp(popUpView);
                    return true;

                case R.id.menu_help:
                    startActivity(new Intent(this, HelpActivity.class));
                    return true;

                case R.id.menu_settings:
                    startActivity(new Intent(this, SettingsActivity.class));
                    return true;

                default:
                    return superMenu;
            }
        } else {
            return superMenu;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_add_group:
                GroupDialogFragment groupDialog = GroupDialogFragment.newInstance(null);
                groupDialog.show(getSupportFragmentManager(), DIALOG_NEW_GROUP);
                groupDialog.setOnActionCompleteListener(new BaseActionDialogFragment.OnActionCompleteListener() {
                    @Override
                    public void onActionCompleted() {
                        groupsFragment.updateGroups();
                    }
                });
                return true;

            case R.id.menu_add_cell:
                CellDialogFragment cellDialog = CellDialogFragment.newInstance(null, IntConstants.DIALOG_NEW_CELL);
                cellDialog.show(getSupportFragmentManager(), DIALOG_NEW_CELL);
                cellDialog.setOnActionCompleteListener(new BaseActionDialogFragment.OnActionCompleteListener() {
                    @Override
                    public void onActionCompleted() {
                        listFragment.refreshAdapter();
                    }
                });
                return true;

            default:
                return false;

        }
    }

    private void showPopUp(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_add_popup, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    private boolean isLandscape() {
        return Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation;
    }

}
