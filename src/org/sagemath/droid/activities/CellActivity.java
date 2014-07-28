package org.sagemath.droid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import org.sagemath.droid.R;
import org.sagemath.droid.fragments.CellGroupsFragment;
import org.sagemath.droid.fragments.CellGroupsFragment.OnGroupSelectedListener;
import org.sagemath.droid.fragments.CellListFragment;
import org.sagemath.droid.models.database.Group;
import org.sagemath.droid.utils.SimpleEula;

/**
 * CellActivity - main activity, first screen
 *
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 */
public class CellActivity
        extends ActionBarActivity
        implements OnGroupSelectedListener {
    private final static String TAG = "SageDroid:CellActivity";
    private static final String DIALOG_NEW_GROUP = "newGroup";

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
            //this.finish();
        }

        groupsFragment = (CellGroupsFragment)
                getSupportFragmentManager().findFragmentById(R.id.cell_groups_fragment);
        groupsFragment.setOnGroupSelected(this);

        listFragment = (CellListFragment)
                getSupportFragmentManager().findFragmentById(R.id.cell_list_fragment);

    }

    public static final String INTENT_SWITCH_GROUP = "intent_switch_group";

    @Override
    public void onGroupSelected(Group group) {
        CellListFragment listFragment = (CellListFragment)
                getSupportFragmentManager().findFragmentById(R.id.cell_list_fragment);
        if (listFragment == null || !listFragment.isInLayout()) {
            Intent i = new Intent(getApplicationContext(), CellListActivity.class);
            i.putExtra(INTENT_SWITCH_GROUP, group);
            startActivity(i);
        } else {
            listFragment.setGroup(group);
        }
    }

}
