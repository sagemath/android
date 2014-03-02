package org.sagemath.droid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by AruL on 2/3/14.
 */
public class Welcome extends Activity implements View.OnClickListener {

    Button tutorial,abt,ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        tutorial = (Button) findViewById(R.id.tuts);
        abt = (Button) findViewById(R.id.about);
        ref = (Button) findViewById(R.id.ref);
        tutorial.setOnClickListener(this);
        abt.setOnClickListener(this);
        ref.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(view == tutorial){
            Uri uri = Uri.parse("http://www.sagemath.org/doc/tutorial");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }else if(view == abt){
            Uri uri = Uri.parse("http://www.sagemath.org");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if(view == ref){
            Uri uri = Uri.parse("http://www.sagemath.org/doc/reference/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

    }
}
