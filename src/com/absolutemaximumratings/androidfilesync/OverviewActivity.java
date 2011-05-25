package com.absolutemaximumratings.androidfilesync;

import java.util.concurrent.atomic.AtomicLong;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.absolutemaximumratings.androidfilesync.Synchronizer.SynchroBinder;

public class OverviewActivity extends Activity {
	TextView statusText;
	boolean synchroBound = false;
	public Synchronizer synchro;
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
	
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	// Bind the synchronizer service   	   	
    	Log.i("filesync", "a");
    	Log.i("filesync", "overview onstart");
    	Intent intent = new Intent(this, Synchronizer.class);
    	
    	Log.i("filesync", "try bind");
    	getApplicationContext().bindService(intent, synchroConnection, Context.BIND_AUTO_CREATE);
    	
    }
	
    @Override
    protected void onStop() {
    	super.onStop();
    	// Unbind the synchro service
    	
    	if (synchroBound) {
    		getApplicationContext().unbindService(synchroConnection);
    		synchroBound = false;
    	}
    }
	
    private OnClickListener btSynchronize_Click = new OnClickListener() {
        public void onClick(View v) {
        	Context context = getApplicationContext();
        	Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
        	
            if (synchroBound) {
            	Log.i("filesync", "activity calling synchro");
            	
            	Toast.makeText(context, "calling in synchronizer", Toast.LENGTH_SHORT).show();
            	synchro.startSynchronize();
            } else {
            	Log.e("filesync", "can't start synchro; not bound");
            	Toast.makeText(context, "no synchronizer bound", Toast.LENGTH_SHORT).show();
            }
            
        }   
    };
    
    
    
    private ServiceConnection synchroConnection = new ServiceConnection() {
    	
    	public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
    		Log.i("filesync", "service connected");
    		SynchroBinder synchroBinder = (SynchroBinder) serviceBinder;
    		synchro = synchroBinder.getService();
    		synchroBound = true;
    		statusText.setText("Connected to Synchronizer\nNew Line?");
		}

		public void onServiceDisconnected(ComponentName name) {
			Log.i("filesync", "service disconnected");
			synchroBound = false;
			statusText.setText("Could not connect to Synchronizer");
		}
    };
}
