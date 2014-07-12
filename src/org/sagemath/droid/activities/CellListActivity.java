package org.sagemath.droid.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import org.sagemath.droid.R;
import org.sagemath.droid.dialogs.NewCellDialogFragment;
import org.sagemath.droid.fragments.CellListFragment;
import org.sagemath.droid.utils.ChangeLog;


/**
 * CellListActivity - when the CellListFragment has its own activity (phones)
 *
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 */
public class CellListActivity extends ActionBarActivity {
    private static final String TAG = "SageDroid:CellListActivity";
    private static String ARG_GROUP = "group";

    private static final String DIALOG_NEW_CELL = "newCell";
    private ChangeLog changeLog;
    private String group;

    private CellListFragment cellListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cell_list);

        cellListFragment = (CellListFragment)
                getSupportFragmentManager().findFragmentById(R.id.cell_list_fragment);

        Intent intent = getIntent();
        if (intent == null)
            cellListFragment.switchToGroup(null);
        else {
            group = intent.getStringExtra(CellActivity.INTENT_SWITCH_GROUP);
            Log.i(TAG, "Got group:" + group);
            cellListFragment.setGroup(group);
        }

        if (savedInstanceState != null) {
            group = savedInstanceState.getString(ARG_GROUP);
            cellListFragment.setGroup(group);
        }

        setTitle(group);

        changeLog = new ChangeLog(this);
        if (changeLog.firstRun())
            changeLog.getLogDialog().show();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_GROUP, group);
    }

    @Override
    public void onResume() {
        super.onResume();
        cellListFragment.setGroup(group);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.sparse, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Uri uri;
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_add: {
                FragmentManager fm = this.getSupportFragmentManager();
                NewCellDialogFragment dialog = NewCellDialogFragment.newInstance();
                dialog.setOnCellCreateListener(new NewCellDialogFragment.OnCellCreateListener() {
                    @Override
                    public void onCellCreated() {
                        cellListFragment.refreshAdapter();
                    }
                });
                dialog.show(fm, DIALOG_NEW_CELL);
                return true;
            }
            case R.id.menu_search:
                Toast.makeText(this, "Tapped search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_changelog:
                changeLog.getFullLogDialog().show();
                return true;
            case R.id.menu_about_sage:
                uri = Uri.parse("http://www.sagemath.org");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            case R.id.menu_manual_user:
                uri = Uri.parse("http://www.sagemath.org/doc/tutorial/");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            case R.id.menu_manual_dev:
                uri = Uri.parse("http://www.sagemath.org/doc/reference/");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            case R.id.menu_clean_history:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
