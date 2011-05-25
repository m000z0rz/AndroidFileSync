package com.absolutemaximumratings.androidfilesync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.RemoteViews;


public class Synchronizer extends Service {
	private HandlerThread _thread;
	private Looper _threadLooper;
	private ThreadHandler _threadHandler;
	private final IBinder _binder = new SynchroBinder();
	private NotificationManager _nm;
	private final int _notificationId = 1; 
	private Notification _notification;
	private final int _endNotificationId = 2;
	private Notification _endNotification;
	
	private SynchroSQL synchroSQL = null;
		
	public class SynchroBinder extends Binder {
		Synchronizer getService() {
			return Synchronizer.this;
		}
		
	}

	private final class ThreadHandler extends Handler {
		public ThreadHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			//do thread work here
			Log.i("filesync", "handleMessage with what=" + msg.what);
			if(msg.what==1) synchronize();
			//Log.i("filesync", "handleMessage");
			//stopSelf(msg.arg1);
		}
		
		
	}
	
	public class SynchroFile {
		public String localPath;
		public String remotePath;
		public long sizeInBytes;
		public int syncType; //1-push 2-pull
		
		FTPClient ftp;
		
		public void transfer() throws Exception {
			File file = new File(localPath);
			if(syncType==1) {
				Log.i("filesync", "pushing " + localPath + " to " + remotePath);
				//push
			   InputStream in = null;
			   try {
				   in = new BufferedInputStream(new FileInputStream(file));
				   //in = new FileInputStream(file);
				   ftp.storeFile(remotePath, in);
				   in.close();
				   /*
				   if (!ftp.completePendingCommand()) {
					   Log.e("filesync", "problem completing pending command on push " + localPath);
				   }
				   */
				   
			   } catch (Exception ex) {
				   Log.e("filesync", "exception pushing file " + localPath + " - " + ex.getMessage());
			   } finally {
				   if (in != null) in.close();
			   }
			   

			// pull
			} else if (syncType==2) {
				Log.i("filesync", "pulling " + remotePath + " to " + localPath);
				OutputStream out = null;
				try {
					file.getParentFile().mkdirs();
					
					out = new BufferedOutputStream(new FileOutputStream(file));
					ftp.retrieveFile(remotePath, out);
					out.close();
					/*
					if (!ftp.completePendingCommand()) {
						   Log.e("filesync", "problem completing pending command on pull " + remotePath);
					   }
					   */
				} catch (Exception ex) {
					Log.e("filesync", "exception pulling file " + remotePath + " - " + ex.getMessage());
				} finally {
					if (out != null) out.close();
				}
				
			}
		}
	}
	
	private void synchronize() {
		AtomicLong totalBytes = new AtomicLong(0);
		long transferredBytes;
		transferredBytes = 0;
		
		
		Log.i("filesync", "starting main synchronize");
		
		WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifi.getConnectionInfo();
		if(!wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED)){
			return;
		}
		
		String ssid = wifiInfo.getSSID();
		
		SQLiteDatabase db = synchroSQL.getReadableDatabase();
		
		int server_id=-1;
		String hostname="";
		String username="";
		String password="";
		
		Cursor servers = null;
		String select = "SELECT * FROM servers WHERE ssid=?";
		try {
			servers = db.rawQuery(select, new String[] {ssid});
		} catch (Exception ex) {
			Log.e("filesync", "sql get server exception: " + ex.getMessage());
		}
		if(servers.getCount()!=1) {
			//error basically
		} else {
			servers.moveToFirst();
			server_id = servers.getInt(servers.getColumnIndex("server_id"));
			hostname = servers.getString(servers.getColumnIndex("hostname"));
			username = servers.getString(servers.getColumnIndex("username"));
			password = servers.getString(servers.getColumnIndex("password"));
		}
		

			
			
					
			
			
			
		Cursor folders = null;
		select = "SELECT * \n"
			+ "FROM folders\n"
			+ "WHERE folder_id IN (\n"
			+ "    SELECT folder_id\n"
			+ "    FROM synchronizations\n"
			+ "    WHERE synchronizations.server_id=?\n"
			+ ")\n";
		try {
			folders = db.rawQuery(select, new String[] {"" + server_id});
		} catch (Exception ex) {
			Log.e("filesync", "sql get folders exception: " + ex.getMessage());
		}
		//Log.i("filesync", "folders count: " + folders.getCount());
		
		if(folders.getCount()==0) {
			return;
		}
		
		// at this point, we know we have folders we could be uploading.  Go ahead and make the notification that
		// we're pending
		PendingIntent notificationIntent = null;
		RemoteViews notificationView = null;
		
		_notification = null;
		//show notification
		try {
			_notification = new Notification(R.drawable.notify_icon, "FileSync starting", System.currentTimeMillis());
			notificationIntent = PendingIntent.getActivity(this, 0, new Intent(this, AndroidFileSync.class), 0);
			//_;notification.defaults |= Notification.DEFAULT_LIGHTS;
			_notification.ledARGB=0xffff9000;
			_notification.ledOnMS = 300;
			_notification.ledOffMS = 1000;
			_notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			_notification.flags |= Notification.FLAG_ONGOING_EVENT;
			_notification.flags |= Notification.FLAG_NO_CLEAR;
			//_notification.setLatestEventInfo(this, "FileSync", "PreparingOld...", notificationIntent);
			
			notificationView = new RemoteViews(getPackageName(), R.layout.notification_layout);
			notificationView.setTextViewText(R.id.notifyText, "Preparing...");
			notificationView.setProgressBar(R.id.notifyProgress, 1, 0, true);
			_notification.contentView = notificationView;
			_notification.contentIntent = notificationIntent;
			
			_nm.notify(_notificationId, _notification);
			
		} catch (Exception ex) {
			Log.e("filesync", "notification exception: " + ex.getMessage());
		}
		
		
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		} else {
			cancelNotification("FileSync aborted", "FileSync aborted: did not have full access to external storage");
			return;
		}
		
		
		File localRoot = Environment.getExternalStorageDirectory() ;
		
        FTPClient ftp;
        boolean error = false;
        
        try {
        	ftp = new FTPClient();
        	ftp.connect(hostname,52);
        	if(!ftp.login(username, password)) {
        		ftp.logout();
        		error = true;
        		Log.i("filesync", "bad login :(");
        		cancelNotification("FileSync aborted", "Could not log in to FTP: " + ftp.getReplyString());
        		return;
        	}
        	ftp.setFileType(FTP.BINARY_FILE_TYPE);
        	//Log.i("filesync", "default control enc: " + ftp.getControlEncoding());
        	ftp.setControlEncoding("UTF-8");
        	ftp.enterLocalPassiveMode();
        } catch (IOException e) {
        	Log.e("filesync", "Error connecting FTP: " + e.getMessage());
        	cancelNotification("FileSync aborted", "Exception connecting to FTP: " + e.getMessage());
        	return;
        }
		
        
        folders.moveToFirst();
        
        String folderLocalPath;
        String folderRemotePath;
        int folderType;
        
        ArrayList<SynchroFile> filesToSync = new ArrayList<SynchroFile>();
        
        
        while(!folders.isAfterLast()) {
        	folderLocalPath = folders.getString(folders.getColumnIndex("local_path"));
        	folderRemotePath = folders.getString(folders.getColumnIndex("remote_path"));
        	folderType = folders.getInt(folders.getColumnIndex("type"));
        	
        	findFilesToSync(localRoot.getAbsolutePath() + "/" + folderLocalPath, folderRemotePath, folderType, ftp, filesToSync, totalBytes);
        	
        	folders.moveToNext();
        }
        

        
        if(filesToSync.size()==0) {
        	// no files to sync; just forget about it
        	_nm.cancel(_notificationId);
        	return;
        }
        
        Log.i("filesync", "Files to sync: ");
        for (int i=0; i<filesToSync.size(); i++) {
        	Log.i("filesync", "  " + i + ": " + filesToSync.get(i).localPath + "^^^" + filesToSync.get(i).remotePath);
        }
        
        SynchroFile synchroFile;
        for (int i=0; i<filesToSync.size(); i++) {
        	synchroFile=filesToSync.get(i);
        	synchroFile.ftp = ftp;
        	File localFile = new File(synchroFile.localPath);
        	String action = "";
        	if(synchroFile.syncType==1) {
        		action = "Pushing ";
        	} else if (synchroFile.syncType==2) {
        		action = "Pulling ";
        	}
        	notificationView.setTextViewText(R.id.notifyText, action + localFile.getName());
        	notificationView.setProgressBar(R.id.notifyProgress, totalBytes.intValue(), (int) transferredBytes, false);
        	_nm.notify(_notificationId, _notification);
        	try {
        		synchroFile.transfer();
        	} catch (Exception ex) {
        		Log.e("filesync", "exception transferring file: " + ex.getMessage());
        	}
        	transferredBytes += synchroFile.sizeInBytes;
        }
        
        
        
        
        
        
        try {
			ftp.disconnect();
		} catch (IOException ex) {
			Log.e("filesync", "IOException on FTP disconnect: " + ex.getMessage());
		}
		
		
        // all done!
        try {
        	cancelNotification("FileSync Complete", "FileSync complete");
        } catch (Exception ex) {
        	Log.e("filesync", "exception completing notification: " + ex.getMessage());
        }

		

		stopSelf();
	}
	
	private void cancelNotification(String cancelPopup, String cancelText) {
		_nm.cancel(_notificationId);
		_endNotification = new Notification(R.drawable.notify_icon, cancelPopup, System.currentTimeMillis());
		_endNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		_endNotification.setLatestEventInfo(this, "FileSync", cancelText, null);
		//_endNotification.contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
		_endNotification.contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, AndroidFileSync.class), 0);
		_nm.notify(_endNotificationId, _endNotification);
	}
	
	private void findFilesToSync(String localPath, String remotePath, int type, FTPClient ftp, ArrayList<SynchroFile> filesToSync, AtomicLong totalBytes ) {
		ArrayList<String> checkedFolders = new ArrayList<String>(); //local paths already checked
		//Log.i("filesync", "findFiles called for local '" + localPath + "', remote '" + remotePath + "'");
		
		try {
		 // push or both - loop through local
			if ((type==1)||(type==3)) {
				File localDirectory = new File(localPath);
				if(localDirectory.exists()) {
					File[] subFiles = localDirectory.listFiles();
					File curFile;
					
					String[] remoteNamesArray = ftp.listNames(remotePath);
					ArrayList<String> remoteNames = new ArrayList<String>();
					for (int i =0; i<remoteNamesArray.length; i++) {
						//Log.i("filesync", "findFiles found remote file name '" + remoteNamesArray[i] + "'");
						remoteNames.add(remoteNamesArray[i]);
					}
					
					for (int i=0; i<subFiles.length; i++) {
						//Log.i("filesync", "findFiles checking local file '" + subFiles[i].getName() + "'");
						curFile = subFiles[i];
						if(curFile.isDirectory()) {
							//Log.i("filesync", "findFiles going to subfolder");
							if(!remoteNames.contains(remotePath + "/" + curFile.getName())) {
								//Log.i("filesync", "findFiles need to make remote");
								ftp.makeDirectory(remotePath + "/" + curFile.getName());
							}
							findFilesToSync(curFile.getAbsolutePath(), remotePath + "/" + curFile.getName(), type, ftp, filesToSync, totalBytes);
							checkedFolders.add(curFile.getAbsolutePath());
						} else {
							if(!remoteNames.contains(remotePath + "/" + curFile.getName())) {
								//Log.i("filesync", "findFiles adding " + curFile.getAbsolutePath() + " >>> " + remotePath + "/" + curFile.getName() + ", " + curFile.length() + " bytes");
								SynchroFile synchroFile = new SynchroFile();
								synchroFile.localPath = curFile.getAbsolutePath();
								synchroFile.remotePath = remotePath + "/" + curFile.getName();
								synchroFile.syncType = 1; //push
								synchroFile.sizeInBytes = curFile.length();
								totalBytes.set(totalBytes.get() + synchroFile.sizeInBytes);
								//Log.i("filesync", "tbytes: " + totalBytes);
								filesToSync.add(synchroFile);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			Log.e("filesync", "exception finding to push: " + ex.getMessage() + "\n stack: " + ex.getStackTrace().toString());
		}
		//pull or both - loop through remote
		try {
			if ((type==2)||(type==3)) {
				//File localDirectory = new File(localPath);
				FTPFile[] subFiles = ftp.listFiles(remotePath);
				FTPFile curFile = null;
				
				File localDirectory = new File(localPath);
				ArrayList<String> localNames = new ArrayList<String>();
				
				
				if(localDirectory.exists()) {
					String[] localNamesArray = localDirectory.list();
					for (int i =0; i<localNamesArray.length; i++) {
						//Log.i("filesync", "findFiles pull found local file " + localNamesArray[i]);
						localNames.add(localNamesArray[i]);
					}
				}
				
				for (int i=0; i<subFiles.length; i++) {
					curFile = subFiles[i];
					//Log.i("filesync", "findFiles pull checking remote file " + subFiles[i].getName());
					if(curFile.isDirectory()) {
						if(!checkedFolders.contains(localPath + "/" + curFile.getName())) {
							//Log.i("filesync", "findFiles pull doing subfolder");
							//if local doesn't exist, will be made later automatically?
							findFilesToSync(localPath + "/" + curFile.getName(), remotePath + "/" + curFile.getName(), type, ftp, filesToSync, totalBytes);	
						} else {
							//Log.i("filesync", "already chedked folder " + "/" + curFile.getName());
						}
						
					} else {
						if(!localNames.contains(curFile.getName())) {
							//Log.i("filesync", "findFiles pull adding " + remotePath + "/" + curFile.getName() + " >>> " + localPath + "/" + curFile.getName() + ", " + curFile.getSize() + " bytes");
							SynchroFile synchroFile = new SynchroFile();
							synchroFile.localPath = localPath + "/" + curFile.getName();
							synchroFile.remotePath = remotePath + "/" + curFile.getName();
							synchroFile.syncType = 2; //pull
							synchroFile.sizeInBytes = curFile.getSize();
							totalBytes.set(totalBytes.get() + synchroFile.sizeInBytes);
							//Log.i("filesync", "tbytes: " + totalBytes);
							filesToSync.add(synchroFile);
						}
					}
				}
			}
		} catch (Exception ex) {
			Log.e("filesync", "exception finding to pull: " + ex.getMessage());
		}

	}
	
	@Override
	public void onCreate() {
		Log.i("filesync", "service onCreate");
		_thread = new HandlerThread("DoesThisNameReallyMatter", Process.THREAD_PRIORITY_BACKGROUND);
		_thread.start();
		
		_threadLooper = _thread.getLooper();
		_threadHandler = new ThreadHandler(_threadLooper);
		
		_nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		synchroSQL = new SynchroSQL(getApplicationContext());
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startSynchronize();
		
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return _binder;
	}
	
	public void startSynchronize() {
		Message msg = _threadHandler.obtainMessage();
		msg.what = 1; // start
		//msg.arg1 = startId;
		_threadHandler.sendMessage(msg);
	}
	
	
	@Override
	public void onDestroy() {
		Log.i("filesync", "service onDestory");
		_nm.cancel(_notificationId);
		//Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
		_thread.quit();
	}

}
