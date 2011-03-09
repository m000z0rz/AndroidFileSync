package com.absolutemaximumratings.androidfilesync;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
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

import org.apache.commons.net.ftp.*;

import com.absolutemaximumratings.androidfilesync.Synchronizer.SynchroBinder;


//import org.apache.commons.net.ftp.FTPClient;

public class AndroidFileSync extends Activity {
	Synchronizer synchro;
	boolean synchroBound = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        
        LinearLayout layout = new LinearLayout(context);
        TextView headerText = new TextView(context);
        headerText.append("hellos!\nmeow");
        layout.addView(headerText);
        Button btSynchronize = new Button(context);
        btSynchronize.setText("Begin Synchronize");
        btSynchronize.setOnClickListener(btSynchronize_Click);
        setContentView(layout);
        
        /*
        Context context = getApplicationContext();
        TextView view = new TextView(context);
        view.setText("hellos");
        setContentView(view);
        
        boolean error;
        
        error = false;
        FTPClient ftp;
        
        try {
        	ftp = new FTPClient();
        	view.append("a");
        	Log.i("filesync", "a");
        	//Log.i("filesync", "address: " + InetAddress.getByName("96.42.47.246").toString());
        	ftp.connect("192.168.1.101",52);
        	Log.i("filesync", "b");
        	
        	if(!ftp.login("m000z0rz", "s31f3rTX")) {
        		ftp.logout();
        		error = true;
        	} else {
        		view.append("logged in");
        	}
        	//ftpClient.changeWorkingDirectory("");
        	//ftp.setFileType(FTP.BINARY_FILE_TYPE);
        	//BufferedInputStream buffIn=null;
        	//buffIn = new BufferedInputStream(new FileInputStream(file));
        	ftp.enterLocalPassiveMode();
        	Log.i("filesync", "Remote system is " + ftp.getSystemType());
        } catch (IOException e) {
        	Log.e("filesync", "Error connecting: " + e.getMessage());
        	return;
        }

        if(error) {
        	Log.e("filesync", "Error logging in");
        	return;
        }

        
        try {
        	String downLocal = "/down.txt";
        	String downRemote = "/down.txt";
        	String upLocal = "/up.txt";
        	String upRemote = "/up.txt";
        	
        	InputStream input;
        	input = new FileInputStream(downLocal);
        	//ftp.storeFile(downRemote, input);
        	input.close();
        	
        	OutputStream output;
        	output = new FileOutputStream(upLocal);
        	//ftp.retrieveFile(upRemote, output);
        	output.close();
        	
        	ftp.logout();
        
        } catch (FTPConnectionClosedException e) {
        	error = true;
        	Log.e("filesync", "Server closed connection.");
        } catch (IOException ex) {
        	error = true;
        	Log.e("filesync", "Error doing ftp stuff: " + ex.getMessage());
        } finally {
        	if (ftp.isConnected()) {
        		try {
        			ftp.disconnect();
        		} catch (IOException f) {
        			
        		}
        	}
        }

        Log.i("filesync", "androidfilesync done");
        */
    }
    
    private OnClickListener btSynchronize_Click = new OnClickListener() {
        public void onClick(View v) {
        	Context context = getApplicationContext();
            if (synchroBound) {
            	Log.i("filesync", "activity calling synchro");
            	
            	Toast.makeText(context, "calling in synchronizer", Toast.LENGTH_SHORT).show();
            	synchro.startSynchronize();
            } else {
            	Log.e("filesync", "not bound");
            	Toast.makeText(context, "no synchronizer bound", Toast.LENGTH_SHORT).show();
            }
        }
    };

    
    @Override
    protected void onStart() {
    	super.onStart();
    	// Bind the synchronizer service
    	Intent intent = new Intent(this, Synchronizer.class);
    	bindService(intent, synchroConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	// Unbind the synchro service
    	if (synchroBound) {
    		unbindService(synchroConnection);
    		synchroBound = false;
    	}
    }
    
    private ServiceConnection synchroConnection = new ServiceConnection() {
    	
    	public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
    		SynchroBinder synchroBinder = (SynchroBinder) serviceBinder;
    		synchro = synchroBinder.getService();
    		synchroBound = true;	
		}

		public void onServiceDisconnected(ComponentName name) {
			synchroBound = false;
		}
    };
}