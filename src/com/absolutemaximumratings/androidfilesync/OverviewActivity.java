package com.absolutemaximumratings.androidfilesync;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OverviewActivity extends Activity {
	TextView statusText;
	
	//Activity masterActivity = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		Button btSynchronize = new Button(this);
		btSynchronize.setText("Synchronize");
		btSynchronize.setOnClickListener(btSynchronize_Click);
		layout.addView(btSynchronize);
		
		statusText = new TextView(this);
		statusText.setText("Connecting to Synchronizer...");
		statusText.setPadding(5,5,5,5);
		layout.addView(statusText);
		
		setContentView(layout);
	}
	
    private OnClickListener btSynchronize_Click = new OnClickListener() {
        public void onClick(View v) {
        	Context context = getApplicationContext();
        	Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
        	/*
            if (synchroBound) {
            	Log.i("filesync", "activity calling synchro");
            	
            	Toast.makeText(context, "calling in synchronizer", Toast.LENGTH_SHORT).show();
            	synchro.startSynchronize();
            } else {
            	Log.e("filesync", "not bound");
            	Toast.makeText(context, "no synchronizer bound", Toast.LENGTH_SHORT).show();
            }
            */
        }
    };
}
