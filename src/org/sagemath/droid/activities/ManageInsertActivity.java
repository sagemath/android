package org.sagemath.droid.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import org.sagemath.droid.R;
import org.sagemath.droid.fragments.ManageInsertFragment;

/**
 * Created by Haven on 19-07-2014.
 */
public class ManageInsertActivity extends ActionBarActivity {
    private static final String TAG = "SageDroid:ManageInsertActivity";

    private ManageInsertFragment insertFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_insert);

    }
}
