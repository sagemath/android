package org.sagemath.droid.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.squareup.otto.Subscribe;
import org.sagemath.droid.R;
import org.sagemath.droid.SageSingleCell;
import org.sagemath.droid.constants.StringConstants;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.dialogs.DeleteCellDialogFragment;
import org.sagemath.droid.dialogs.NewCellDialogFragment;
import org.sagemath.droid.events.CodeReceivedEvent;
import org.sagemath.droid.events.InteractFinishEvent;
import org.sagemath.droid.events.ProgressEvent;
import org.sagemath.droid.events.ServerDisconnectEvent;
import org.sagemath.droid.fragments.CodeEditorFragment;
import org.sagemath.droid.fragments.OutputViewFragment;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.utils.BusProvider;
import org.sagemath.droid.utils.ChangeLog;

/**
 * SageActivity - handling of single cell display and input
 *
 * @author vbraun
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 */
public class SageActivity
        extends
        ActionBarActivity
        implements
        DeleteCellDialogFragment.OnCellDeleteListener,
        ToggleButton.OnCheckedChangeListener {
    private static final String TAG = "SageDroid:SageActivity";

    private static final String DIALOG_NEW_CELL = "newCell";
    private static final String DIALOG_DISCARD_CELL = "discardCell";

    private ChangeLog changeLog;

    private ProgressBar cellProgressBar;
    private SuperCardToast toast;

    private CodeEditorFragment codeEditorFragment;
    private OutputViewFragment outputViewFragment;
    private View dividerView;

    private SageSQLiteOpenHelper helper;

    private static SageSingleCell server;

    private boolean isServerRunning = false;


    private Cell cell;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        server = new SageSingleCell(this);
        helper = SageSQLiteOpenHelper.getInstance(this);
        BusProvider.getInstance().register(this);

        Long cellID = getIntent().getLongExtra(StringConstants.ID, -1);

        setContentView(R.layout.activity_sage);

        dividerView = findViewById(R.id.dividerView);
        codeEditorFragment = (CodeEditorFragment) getSupportFragmentManager().findFragmentById(R.id.codeFragment);
        outputViewFragment = (OutputViewFragment) getSupportFragmentManager().findFragmentById(R.id.outputFragment);

        codeEditorFragment.getCodeViewToggleButton().setOnCheckedChangeListener(this);
        outputViewFragment.getOutputViewToggleButton().setOnCheckedChangeListener(this);

        if (cellID != -1) {
            cell = helper.getCellbyID(cellID);
            codeEditorFragment.setCell(cell);
            outputViewFragment.setCell(cell);
            Log.i(TAG, "Got cell " + cell.toString());
        }

        changeLog = new ChangeLog(this);
        if (changeLog.firstRun())
            changeLog.getLogDialog().show();


        cellProgressBar = (ProgressBar) findViewById(R.id.cell_progress);
        cellProgressBar.setVisibility(View.INVISIBLE);
        server.setOnSageListener(outputViewFragment.getOutputView());

        try {
            Log.i(TAG, "Cell group is: " + cell.getGroup());
            Log.i(TAG, "Cell title is: " + cell.getTitle());
            Log.i(TAG, "Cell uuid is: " + cell.getUUID().toString());
            //Log.i(TAG, "Starting new SageActivity with HTML: " + cell.getHtmlData());
        } catch (Exception e) {
        }

        setTitle(cell.getTitle());
        boolean isInputEmpty = getIntent().getBooleanExtra(StringConstants.FLAG_INPUT_EMPTY, true);
        if (!isInputEmpty) {
            runButton();
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.codeViewStateToggleButton:
                if (isChecked)
                    performFragmentResize(R.id.codeFragment, R.id.outputFragment);
                else
                    performFragmentRestore();
                break;

            case R.id.outputViewStateToggleButton:
                if (isChecked)
                    performFragmentResize(R.id.outputFragment, R.id.codeFragment);
                else
                    performFragmentRestore();
                break;
        }
    }

    private void performFragmentResize(int expand, int collapse) {
        dividerView.setVisibility(View.GONE);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .hide(manager.findFragmentById(collapse))
                .show(manager.findFragmentById(expand))
                .commit();
    }

    private void performFragmentRestore() {
        dividerView.setVisibility(View.VISIBLE);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .show(manager.findFragmentById(R.id.codeFragment))
                .show(manager.findFragmentById(R.id.outputFragment))
                .commit();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem refreshItem = menu.findItem(R.id.menu_refresh);
        MenuItem runStateItem = menu.findItem(R.id.menu_run);
        Drawable refreshIcon = getResources().getDrawable(R.drawable.ic_action_refresh);
        Drawable playIcon = getResources().getDrawable(R.drawable.ic_action_av_play);
        Drawable stopIcon = getResources().getDrawable(R.drawable.ic_action_av_stop);
        if (isServerRunning) {
            refreshIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            runStateItem.setIcon(stopIcon);
        } else {
            runStateItem.setIcon(playIcon);
        }
        refreshItem.setEnabled(!isServerRunning);
        refreshItem.setIcon(refreshIcon);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_refresh:
                runButton();
                return true;
            case R.id.menu_add: {
                FragmentManager fm = this.getSupportFragmentManager();
                NewCellDialogFragment dialog = NewCellDialogFragment.newInstance();
                dialog.show(fm, DIALOG_NEW_CELL);
                return true;
            }
            case R.id.menu_discard: {
                FragmentManager fm = this.getSupportFragmentManager();
                DeleteCellDialogFragment deleteDialogFragment = DeleteCellDialogFragment.newInstance(cell);
                deleteDialogFragment.setOnCellDeleteListener(this);
                deleteDialogFragment.show(fm, DIALOG_DISCARD_CELL);
                return true;
            }
            case R.id.menu_share:
                try {
                    String shareURL = server.getShareURI().toString();
                    Intent share = new Intent(android.content.Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, shareURL);
                    startActivity(share);
                } catch (Exception e) {
                    Log.e(TAG, "Couldn't share for some reason... " + e.getLocalizedMessage());
                    runButton();
                    Toast.makeText(this, "You must run the calculation first! Try sharing again.", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_run:
                runButton();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCellDeleted() {
        finish();
    }

    @Subscribe
    public void onRun(CodeReceivedEvent event) {
        if (event.isForRun()) {
            String query = event.getReceivedCode();
            server.query(query);
        }
    }

    private void runButton() {
        outputViewFragment.getOutputView().clear();
        codeEditorFragment.getCodeView().getEditorText(true);
    }

    @Subscribe
    public void onInteractFinished(InteractFinishEvent event) {
        hideProgress();
    }

    @Subscribe
    public void onInteractDisconnected(ServerDisconnectEvent event) {
        Log.i(TAG, "Interact Disconnected, Showing Message");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast = new SuperCardToast(SageActivity.this);
                toast.setText(getResources().getString(R.string.info_disconnected));
                toast.setBackground(SuperToast.Background.RED);
                toast.setTextColor(Color.WHITE);
                toast.setDuration(3000);
                toast.setIcon(android.R.drawable.ic_dialog_alert, SuperToast.IconPosition.LEFT);
                toast.setSwipeToDismiss(true);
                toast.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void showProgress() {
        if (!cellProgressBar.isShown()) {
            cellProgressBar.setVisibility(View.VISIBLE);
            isServerRunning = true;
            ActivityCompat.invalidateOptionsMenu(this);
        }
    }

    public void hideProgress() {
        if (cellProgressBar.isShown()) {
            cellProgressBar.setVisibility(View.INVISIBLE);
            isServerRunning = false;
            ActivityCompat.invalidateOptionsMenu(this);
        }
    }

    @Subscribe
    public void onProgressUpdate(ProgressEvent progressEvent) {
        Log.i(TAG, "Received Progress Update: " + progressEvent.getProgressState());
        if (progressEvent.getProgressState().equals(StringConstants.ARG_PROGRESS_START)) {
            showProgress();
        } else if (progressEvent.getProgressState().equals(StringConstants.ARG_PROGRESS_END)) {
            hideProgress();
        }
    }

}
