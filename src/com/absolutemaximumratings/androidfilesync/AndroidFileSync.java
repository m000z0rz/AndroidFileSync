package com.absolutemaximumratings.androidfilesync;

import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.absolutemaximumratings.androidfilesync.Synchronizer.SynchroBinder;


//import org.apache.commons.net.ftp.FTPClient;

public class AndroidFileSync extends TabActivity {
	public Synchronizer synchro;
	public boolean synchroBound = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Resources res = getResources();
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;
        
        intent = new Intent().setClass(this, OverviewActivity.class);
        
        spec = tabHost.newTabSpec("overview").setIndicator("Overview",
        		res.getDrawable(R.drawable.ic_tab_overview))
        		.setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, ServersActivity.class);
        spec = tabHost.newTabSpec("servers").setIndicator("Servers",
        		res.getDrawable(R.drawable.ic_tab_servers))
        		.setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, FoldersActivity.class);
        spec = tabHost.newTabSpec("folders").setIndicator("Folders",
        		res.getDrawable(R.drawable.ic_tab_folders))
        		.setContent(intent);
        tabHost.addTab(spec);
        
        tabHost.setCurrentTab(0);
        
        
        /*
        
        Context context = getApplicationContext();
        
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView headerText = new TextView(context);
        headerText.append("hellos!\nmeow");
        layout.addView(headerText);
        
        Button btSynchronize = new Button(context);
        btSynchronize.setText("Begin Synchronize");
        btSynchronize.setOnClickListener(btSynchronize_Click);
        layout.addView(btSynchronize);
        
        setContentView(layout);
        
        
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
    	Log.i("filesync", "filesync onstart");
    	//bindService(intent, synchroConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	// Unbind the synchro service
    	/*
    	if (synchroBound) {
    		unbindService(synchroConnection);
    		synchroBound = false;
    	}
    	*/
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