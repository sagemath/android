package org.sagemath.droid.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;
import junit.framework.Assert;
import org.sagemath.droid.OutputView;
import org.sagemath.droid.R;
import org.sagemath.droid.SageSingleCell;
import org.sagemath.droid.cells.CellCollection;
import org.sagemath.droid.constants.StringConstants;
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.dialogs.NewCellDialogFragment;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.models.gson.InteractReply;
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
        Button.OnClickListener,
        OutputView.onSageListener,
        SageSingleCell.OnSageDisconnectListener,
        AdapterView.OnItemSelectedListener {
    private static final String TAG = "SageDroid:SageActivity";

    private static final String DIALOG_NEW_CELL = "newCell";
    private static final String DIALOG_DISCARD_CELL = "discardCell";
    private static final String ARG_HTML = "html";
    private static final String ARG_BUNDLE = "bundle";

    protected static final int INSERT_FOR_LOOP = 1;
    protected static final int INSERT_LIST_COMPREHENSION = 2;

    private ChangeLog changeLog;

    private EditText input;
    private Button roundBracket, squareBracket, curlyBracket;
    private ImageButton runButton;
    private Spinner insertSpinner;
    private OutputView outputView;
    private ProgressBar cellProgressBar;
    private SuperCardToast toast;

    private SageSQLiteOpenHelper helper;

    private static SageSingleCell server;

    private boolean isServerRunning = false;
    private String savedHtml;
    private Bundle savedData;

    private Cell cell;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(this).registerReceiver(progressBroadcastReceiver, new IntentFilter(StringConstants.PROGRESS_INTENT));
        server = new SageSingleCell(this);
        helper = SageSQLiteOpenHelper.getInstance(this);

        Long cellID = getIntent().getLongExtra(StringConstants.ID, -1);

        if (cellID != -1) {
            cell = helper.getCellbyID(cellID);
        }

        setContentView(R.layout.main);

        changeLog = new ChangeLog(this);
        if (changeLog.firstRun())
            changeLog.getLogDialog().show();

        input = (EditText) findViewById(R.id.sage_input);
        roundBracket = (Button) findViewById(R.id.bracket_round);
        squareBracket = (Button) findViewById(R.id.bracket_square);
        curlyBracket = (Button) findViewById(R.id.bracket_curly);
        runButton = (ImageButton) findViewById(R.id.button_run);
        outputView = (OutputView) findViewById(R.id.sage_output);
        insertSpinner = (Spinner) findViewById(R.id.insert_text);
        cellProgressBar = (ProgressBar) findViewById(R.id.cell_progress);
        cellProgressBar.setVisibility(View.INVISIBLE);
        server.setOnSageListener(outputView);
        server.setOnSageDisconnectListener(this);

        outputView.setOnSageListener(this);
        outputView.setCell(cell);
        insertSpinner.setOnItemSelectedListener(this);
        roundBracket.setOnClickListener(this);
        squareBracket.setOnClickListener(this);
        curlyBracket.setOnClickListener(this);
        runButton.setOnClickListener(this);

        //We have saved HTML, load it
        if (savedInstanceState != null) {
            Bundle saveState = savedInstanceState.getBundle(ARG_BUNDLE);
            if ((saveState != null) && (saveState.get(ARG_HTML) != null)) {
                savedHtml = saveState.getString(ARG_HTML);
                outputView.setSavedHtml(savedHtml);
            }
        }

        try {
            Log.i(TAG, "Cell group is: " + cell.getGroup());
            Log.i(TAG, "Cell title is: " + cell.getTitle());
            Log.i(TAG, "Cell uuid is: " + cell.getUUID().toString());
            //Log.i(TAG, "Starting new SageActivity with HTML: " + cell.getHtmlData());
        } catch (Exception e) {
        }

        if (cell.getGroup().equals("History")) {
        } else {
            try {
                outputView.clear();
            } catch (Exception e) {
                Log.e(TAG, "Error clearing output view." + e.getLocalizedMessage());
            }
        }

        //server.setDownloadDataFiles(false);
        setTitle(cell.getGroup() + " • " + cell.getTitle());
        input.setText(cell.getInput());
        Boolean isNewCell = getIntent().getBooleanExtra("NEWCELL", false);
        if (isNewCell) {
            runButton();
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem refreshItem = menu.findItem(R.id.menu_refresh);
        Drawable refreshIcon = getResources().getDrawable(R.drawable.ic_action_refresh);
        if (isServerRunning)
            refreshIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);

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
        Uri uri;
        Intent intent;
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
                DialogFragment dialog = new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                getActivity());
                        builder.setMessage(R.string.dialog_confirm_discard)
                                .setPositiveButton(R.string.discard,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog, int id) {
                                                helper.deleteCell(cell);
                                                SageActivity.this.onBackPressed();
                                            }
                                        }
                                )
                                .setNegativeButton(R.string.cancel,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog, int id) {
                                                // User cancelled the dialog
                                            }
                                        }
                                );
                        // Create the AlertDialog object and return it
                        return builder.create();
                    }
                };
                dialog.show(fm, DIALOG_DISCARD_CELL);
                return true;
            }
            case R.id.menu_search:
                Toast.makeText(this, "Tapped search", Toast.LENGTH_SHORT).show();
                return true;
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
                CellCollection.getInstance().cleanHistory();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int cursor = input.getSelectionStart();
        switch (v.getId()) {
            case R.id.button_run:
                runButton();
                break;
            case R.id.bracket_round:
                input.getText().insert(cursor, "(  )");
                input.setSelection(cursor + 2);
                break;
            case R.id.bracket_square:
                input.getText().insert(cursor, "[  ]");
                input.setSelection(cursor + 2);
                break;
            case R.id.bracket_curly:
                input.getText().insert(cursor, "{  }");
                input.setSelection(cursor + 2);
                break;
        }
    }


    private void runButton() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        try {
            if (!cell.getGroup().equals("History")) {
                outputView.clear();
                Log.i(TAG, "Called outputView.clear()!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing output...");
        }

        String currentInput = input.getText().toString();
        Assert.assertNotNull(currentInput);
        server.query(currentInput);
        outputView.enableInteractViews();
        cell.setInput(currentInput);
        helper.saveEditedCell(cell);
        // saveCurrentToHistory();
    }

    /*private void saveCurrentToHistory() {
        if (!cell.getGroup().equals("History")) {
            CellData HistoryCell = new CellData(cell);
            HistoryCell.setGroup("History");
            String currentInput = input.getText().toString();
            HistoryCell.setInput(currentInput);
            String shortenedInput = HistoryCell.getInput();
            if (HistoryCell.getInput().length() > 16)
                shortenedInput = shortenedInput.substring(0, 16);
            HistoryCell.setTitle(shortenedInput);
            CellCollection.getInstance().addCell(HistoryCell);
        }
    }*/

    @Override
    public void onSageFinishedListener() {
        outputView.enableInteractViews();
        hideProgress();
    }

    @Override
    public void onSageInteractListener(InteractReply interact, String name, Object value) {
        Log.i(TAG, "onSageInteractListener: " + name + " = " + value);
        showProgress();
        outputView.disableInteractViews();
        server.updateInteract(interact, name, value);
        Log.i(TAG, "onSageInteractListener() called!");
    }

    @Override
    public void onServerDisconnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                outputView.disableInteractViews();
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
    protected void onPause() {
        super.onPause();
        String html = outputView.getSavedHtml();
        if (html != null) {
            savedHtml = html;
            savedData = new Bundle();
            savedData.putString(ARG_HTML, savedHtml);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        outputView.setSavedHtml(savedHtml);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(ARG_BUNDLE, savedData);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {
        if (parent != insertSpinner)
            return;
        int cursor = input.getSelectionStart();
        switch (position) {
            case INSERT_FOR_LOOP:
                input.getText().append("\nfor i in range(0,10):\n     ");
                input.setSelection(input.getText().length());
                break;
            case INSERT_LIST_COMPREHENSION:
                input.getText().insert(cursor, "[ i for i in range(0,10) ]");
                input.setSelection(cursor + 2, cursor + 3);
                break;
        }
        parent.setSelection(0);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    public void showProgress() {
        cellProgressBar.setVisibility(View.VISIBLE);
        isServerRunning = true;
        input.setEnabled(false);
        ActivityCompat.invalidateOptionsMenu(this);
    }

    public void hideProgress() {
        cellProgressBar.setVisibility(View.INVISIBLE);
        isServerRunning = false;
        input.setEnabled(true);
        ActivityCompat.invalidateOptionsMenu(this);
    }

    private BroadcastReceiver progressBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean progressStart = intent.getBooleanExtra(StringConstants.ARG_PROGRESS_START, false);
            boolean progressEnd = intent.getBooleanExtra(StringConstants.ARG_PROGRESS_END, false);

            Log.i(TAG, "Received Broadcast");

            if (progressStart) {
                showProgress();
            } else if (progressEnd) {
                hideProgress();
            }
        }
    };

}
