package org.sagemath.droid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import org.sagemath.droid.R;

/**
 * The Settings Activity
 *
 * @author Nikhil Peter Raj
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference manageInsertPreference = findPreference("manage_insert");
        manageInsertPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(SettingsActivity.this, ManageInsertActivity.class));
                return true;
            }
        });
    }
}
