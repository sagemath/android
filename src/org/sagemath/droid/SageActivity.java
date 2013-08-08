package org.sagemath.droid;

import org.sagemath.singlecellserver.Interact;
import org.sagemath.singlecellserver.SageSingleCell;

import sheetrock.panda.changelog.ChangeLog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.actionbarcompat.ActionBarActivity;

/**
 * The main activity of the Sage app
 * 
 * @author vbraun
 *
 */
public class SageActivity 
	extends 
		ActionBarActivity 
	implements
		Button.OnClickListener,
		OutputView.onSageListener,
		OnItemSelectedListener
	{
	private final static String TAG = "SageActivity";
	
	private ChangeLog changeLog;

	private EditText input;
	private Button roundBracket, squareBracket, curlyBracket;
	private Button runButton;
	private Spinner insertSpinner;
	private OutputView outputView;
	
	private static SageSingleCell server = new SageSingleCell();
	
	private CellData cell;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		CellCollection.initialize(getApplicationContext());
        cell = CellCollection.getInstance().getCurrentCell();
        
		server.setServer("http://aleph.sagemath.org", "/eval", "/output_poll", "/files");

		setContentView(R.layout.main);
		
		changeLog = new ChangeLog(this);
        if (changeLog.firstRun())
            changeLog.getLogDialog().show();
        
        input = (EditText) findViewById(R.id.sage_input);
        roundBracket  = (Button) findViewById(R.id.bracket_round);
        squareBracket = (Button) findViewById(R.id.bracket_square);
        curlyBracket  = (Button) findViewById(R.id.bracket_curly);        
        runButton = (Button) findViewById(R.id.button_run);
        outputView = (OutputView) findViewById(R.id.sage_output);
        insertSpinner = (Spinner) findViewById(R.id.insert_text);
        server.setOnSageListener(outputView);

        outputView.setOnSageListener(this);
        insertSpinner.setOnItemSelectedListener(this);
        roundBracket.setOnClickListener(this);
        squareBracket.setOnClickListener(this);
        curlyBracket.setOnClickListener(this);
        runButton.setOnClickListener(this);    	
        
        server.setDownloadDataFiles(false);
        setTitle(cell.getTitle());
        if (server.isRunning())
            getActionBarHelper().setRefreshActionItemState(true);    

        input.setText(cell.getInput());
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
            case R.id.menu_search:
                Toast.makeText(this, "Tapped search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_share:
                Toast.makeText(this, "Tapped share", Toast.LENGTH_SHORT).show();
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
        		uri = Uri.parse("http://http://www.sagemath.org/doc/reference/");
        		intent = new Intent(Intent.ACTION_VIEW, uri); 
        		startActivity(intent); 
         		return true;
        }
        return super.onOptionsItemSelected(item);
    }

    
//    public void setTitle(String title) {
//    	this.title.setText(title);
//    }
//
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
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
		server.interrupt();
		outputView.clear();
		Log.i(TAG, "Called outputView.clear()!");
		server.query(input.getText().toString());
        getActionBarHelper().setRefreshActionItemState(true);
        outputView.requestFocus();
	}

	@Override
	public void onSageFinishedListener() {
        getActionBarHelper().setRefreshActionItemState(false);
	}


	@Override
	public void onSageInteractListener(Interact interact, String name, Object value) {
		Log.i(TAG, "onSageInteractListener: " + name + " = " + value);
		//outputView.clear();
		//Log.i(TAG, "Interact output view cleared!");
		server.interact(interact, name, value);
		Log.i(TAG, "onSageInteractListener() called!");
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		outputView.onResume();
	}

	protected static final int INSERT_PROMPT = 0;
	protected static final int INSERT_FOR_LOOP = 1;
	protected static final int INSERT_LIST_COMPREHENSION = 2; 
	
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
			input.setSelection(cursor+2, cursor+3);
			break;
		}
		parent.setSelection(0);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
}