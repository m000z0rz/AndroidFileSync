package com.absolutemaximumratings.androidfilesync;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;


public class Synchronizer extends Service {
	private Looper _threadLooper;
	private ThreadHandler _threadHandler;
	private final IBinder _binder = new SynchroBinder();
	
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
			Log.i("filesync", "handleMessage");
			stopSelf(msg.arg1);
		}
	}
	
	@Override
	public void onCreate() {
		startSynchronize();
	}
	
	public void startSynchronize() {
		HandlerThread thread = new HandlerThread("DoesThisNameReallyMatter", Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		
		_threadLooper = thread.getLooper();
		_threadHandler = new ThreadHandler(_threadLooper);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
		Log.i("filesync", "onStartCommand");
		
		Message msg = _threadHandler.obtainMessage();
		msg.what = 1; // start
		msg.arg1 = startId;
		_threadHandler.sendMessage(msg);
		
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return _binder;
	}
	
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
	}

}
