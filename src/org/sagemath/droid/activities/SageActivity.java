package org.sagemath.droid.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.ToggleButton;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.squareup.otto.Subscribe;
import org.sagemath.droid.R;
import org.sagemath.droid.constants.StringConstants;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.dialogs.DeleteCellDialogFragment;
import org.sagemath.droid.dialogs.InsertDialogFragment;
import org.sagemath.droid.dialogs.InsertSpinnerDialogFragment;
import org.sagemath.droid.dialogs.ShareDialogFragment;
import org.sagemath.droid.events.*;
import org.sagemath.droid.fragments.AsyncTaskFragment;
import org.sagemath.droid.fragments.CellGroupsFragment;
import org.sagemath.droid.fragments.CodeEditorFragment;
import org.sagemath.droid.fragments.OutputViewFragment;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.models.database.Insert;
import org.sagemath.droid.models.gson.BaseReply;
import org.sagemath.droid.utils.BusProvider;
import org.sagemath.droid.utils.ToastUtils;

/**
 * All calculations are performed and displayed here
 *
 * @author vbraun
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 * @author Nikhil Peter Raj
 */
public class SageActivity
        extends
        ActionBarActivity
        implements
        DeleteCellDialogFragment.OnDeleteListener,
        ToggleButton.OnCheckedChangeListener,
        AsyncTaskFragment.ServerCallbacks
        , ShareDialogFragment.OnRequestOutputListener
        , PopupMenu.OnMenuItemClickListener
        , InsertSpinnerDialogFragment.OnInsertSelectedListener {
    private static final String TAG = "SageDroid:SageActivity";

    private static final String DIALOG_SHARE = "shareDialog";
    private static final String FLAG_SERVER_STATE = "serverState";

    private static final String ARG_ADD_INSERT = "newInsert";
    private static final String ARG_PUT_INSERT = "addInsert";

    private static final String TASK_FRAGMENT_TAG = "taskFragment";

    private ProgressBar cellProgressBar;
    private SuperCardToast toast;

    private Drawable playIcon, stopIcon;
    private Drawable shareIcon, shareEnableIcon;

    private CodeEditorFragment codeEditorFragment;
    private OutputViewFragment outputViewFragment;
    private AsyncTaskFragment taskFragment;

    private View dividerView;

    private SageSQLiteOpenHelper helper;

    private String permalinkURL = null;

    private boolean isServerRunning = false;
    private boolean isShareAvailable = false;
    private boolean isPlayground = false;


    private Cell cell;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helper = SageSQLiteOpenHelper.getInstance(this);
        BusProvider.getInstance().register(this);

        setContentView(R.layout.activity_sage);

        cellProgressBar = (ProgressBar) findViewById(R.id.cell_progress);
        cellProgressBar.setVisibility(View.INVISIBLE);

        dividerView = findViewById(R.id.dividerView);
        codeEditorFragment = (CodeEditorFragment) getSupportFragmentManager()
                .findFragmentById(R.id.codeFragment);
        outputViewFragment = (OutputViewFragment) getSupportFragmentManager()
                .findFragmentById(R.id.outputFragment);

        codeEditorFragment.getCodeViewToggleButton().setOnCheckedChangeListener(this);
        outputViewFragment.getOutputViewToggleButton().setOnCheckedChangeListener(this);

        if (getSupportFragmentManager().findFragmentByTag(TASK_FRAGMENT_TAG) == null) {
            taskFragment = AsyncTaskFragment.getInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(taskFragment, TASK_FRAGMENT_TAG)
                    .commit();
        } else {
            taskFragment = (AsyncTaskFragment) getSupportFragmentManager().findFragmentByTag(TASK_FRAGMENT_TAG);
        }

        Intent intent = getIntent();
        if (intent.hasExtra(CellGroupsFragment.KEY_GROUP_PLAYGROUND)) {
            //Playground setup
            setTitle(getString(R.string.group_playground));
            isPlayground = true;
        } else if (intent.hasExtra(StringConstants.ID)) {

            Long cellID = intent.getLongExtra(StringConstants.ID, -1);

            if (cellID != -1) {
                cell = helper.getCellbyID(cellID);
                codeEditorFragment.setCell(cell);
                outputViewFragment.setCell(cell);
                Log.i(TAG, "Got cell " + cell.toString());
            }

            setTitle(cell.getTitle());

        }
    }

    @Override
    public void onReply(BaseReply reply) {
        BusProvider.getInstance().post(new ReplyEvent(reply));
    }

    @Override
    public void onComputationFinished() {
        hideProgress();
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
        MenuItem shareItem = menu.findItem(R.id.menu_share);
        MenuItem runStateItem = menu.findItem(R.id.menu_run);
        MenuItem saveItem = menu.findItem(R.id.menu_save);
        if (playIcon == null) {
            //Cache to avoid overhead
            playIcon = getResources().getDrawable(R.drawable.ic_action_av_play);
            stopIcon = getResources().getDrawable(R.drawable.ic_action_av_stop);
            shareIcon = getResources().getDrawable(R.drawable.ic_action_social_share);
            shareEnableIcon = getResources().getDrawable(R.drawable.ic_action_social_share_enabled);
        }

        if (isPlayground) {
            saveItem.setEnabled(false);
        }

        if (isServerRunning) {
            runStateItem.setIcon(stopIcon);
        } else {
            runStateItem.setIcon(playIcon);
        }
        if (isShareAvailable && !isServerRunning) {
            shareItem.setIcon(shareEnableIcon);
        } else {
            shareItem.setIcon(shareIcon);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_activity_sage, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_run:
                startExecution();
                return true;
            case R.id.menu_insert:
                View popUpView = findViewById(R.id.menu_insert);
                showPopup(popUpView);
                return true;
            case R.id.menu_save:
                codeEditorFragment.saveCurrentInput();
                return true;
            case R.id.menu_share:
                shareClicked();
                return true;
            case R.id.menu_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    public void showPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_insert_popup, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.menu_insert_spinner:
                InsertSpinnerDialogFragment insertDialog = InsertSpinnerDialogFragment.newInstance();
                insertDialog.show(getSupportFragmentManager(), ARG_PUT_INSERT);
                insertDialog.setOnInsertSelectedListener(this);
                break;

            case R.id.menu_add_insert:
                InsertDialogFragment dialog = InsertDialogFragment.newInstance(null);
                dialog.show(getSupportFragmentManager(), ARG_ADD_INSERT);
                break;

            case R.id.menu_manage_insert:
                startActivity(new Intent(this, ManageInsertActivity.class));
                break;
        }
        return true;
    }

    @Override
    public void onInsertSelected(Insert insert) {
        codeEditorFragment.getCodeView().paste(insert.getInsertText());
    }

    @Override
    public void onDelete() {
        finish();
    }

    @Override
    public void onRequestOutput() {
        Log.i(TAG, "Output Requested");
        outputViewFragment.saveOutputToImage();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onRun(CodeReceivedEvent event) {
        if (event.isForRun()) {
            Log.i(TAG, "Received Query: " + event.getReceivedCode());
            String query = event.getReceivedCode();
            taskFragment.query(query);
        }
    }

    private void shareClicked() {
        if (!isShareAvailable && !isServerRunning) {
            ToastUtils.getAlertToast(this, R.string.toast_share_unavailable, SuperToast.Duration.SHORT).show();
        } else if (isServerRunning) {
            ToastUtils.getAlertToast(this, R.string.toast_share_server_running, SuperToast.Duration.SHORT).show();
        } else {
            ShareDialogFragment shareDialogFragment = ShareDialogFragment.getInstance(permalinkURL);
            shareDialogFragment.show(getSupportFragmentManager(), DIALOG_SHARE);
        }
    }

    private void startExecution() {
        if (isServerRunning) {
            //Computation already running, stop
            cancelComputation();
        } else {
            //Check for Network Connection
            if (!isConnected()) {
                ToastUtils.getAlertToast(this, R.string.toast_network_error, SuperToast.Duration.MEDIUM).show();
            } else {
                isShareAvailable = false;
                isServerRunning = false;
                permalinkURL = null;
                ActivityCompat.invalidateOptionsMenu(this);
                outputViewFragment.getOutputView().clear();
                codeEditorFragment.getCodeView().getEditorText(true);
            }
        }
    }

    private void cancelComputation() {
        isShareAvailable = false;
        ActivityCompat.invalidateOptionsMenu(this);
        hideProgress();
        outputViewFragment.getOutputView().clear();
        taskFragment.cancel();
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onShareEvent(ShareAvailableEvent event) {
        isShareAvailable = true;
        permalinkURL = event.getShareURL();
        ActivityCompat.invalidateOptionsMenu(this);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onInteractFinished(InteractFinishEvent event) {
        hideProgress();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onInteractDisconnected(ServerDisconnectEvent event) {
        Log.i(TAG, "Interact Disconnected, Showing Message");

        //Hide Progress Bar, if it is showing
        hideProgress();

        //Prepare a Toast
        toast = new SuperCardToast((SageActivity.this));
        toast.setBackground(SuperToast.Background.RED);
        toast.setTextColor(Color.BLACK);
        toast.setDuration(3000);
        toast.setIcon(android.R.drawable.ic_dialog_alert, SuperToast.IconPosition.LEFT);
        toast.setSwipeToDismiss(true);

        switch (event.getDisconnectType()) {

            case DISCONNECT_INTERACT:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toast.setText(getString(R.string.toast_error_interact_timeout));
                        toast.show();
                    }
                });
                break;

            case DISCONNECT_SOCKET:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toast.setText(getString(R.string.toast_error_websocket_disconnected));
                        toast.show();
                    }
                });
                break;

            case DISCONNECT_TIMEOUT:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toast.setText(getString(R.string.toast_error_timeout));
                        toast.show();
                    }
                });

        }
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLAG_SERVER_STATE, isServerRunning);
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

    @SuppressWarnings("unused")
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
