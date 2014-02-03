package org.sagemath.droid;

import sheetrock.panda.changelog.ChangeLog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.actionbarcompat.ActionBarActivity;

/**
 * CellListActivity - when the CellListFragment has its own activity (phones)
 * 
 * @author Rasmi.Elasmar
 * @author Ralf.Stephan
 *
 */
public class CellListActivity 
    	extends ActionBarActivity {
	private static final String DIALOG_NEW_CELL = "newCell";
	private ChangeLog changeLog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CellCollection.initialize(getApplicationContext());
		setContentView(R.layout.cell_list_fragment);
		
		CellListFragment listFragment = (CellListFragment)
				getSupportFragmentManager().findFragmentById(R.id.cell_list_fragment);
	
		Intent intent = getIntent();
		if (intent == null)
			listFragment.switchToGroup(null);		
		else {
			String group = intent.getStringExtra(CellActivity.INTENT_SWITCH_GROUP);
			listFragment.switchToGroup(group);		
		}
		
		setTitle(CellCollection.getInstance().getCurrentGroupName());

		changeLog = new ChangeLog(this);
        if (changeLog.firstRun())
            changeLog.getLogDialog().show();
		
}

	
	@Override
	public void onResume() {
		super.onResume();
		if (CellCollection.getInstance().getCurrentGroup().isEmpty())
			this.onBackPressed();
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
			NewCellDialog dialog = new NewCellDialog();
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
			CellCollection.getInstance().cleanHistory();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
