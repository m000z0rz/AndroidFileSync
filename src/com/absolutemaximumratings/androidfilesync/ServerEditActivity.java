package com.absolutemaximumratings.androidfilesync;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class ServerEditActivity extends Activity {
	
	private long id;
	private Cursor mCursor;
	private SynchroSQL synchroSQL = null;
	
	private static final int STATE_INSERT = 0;
	private static final int STATE_EDIT = 1;
	
	private int mState;
	
	private EditText mEditSSID;
	private EditText mEditHostname;
	private EditText mEditUsername;
	private EditText mEditPassword;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i("filesync", "into server edit oncreate");
		
		final Intent intent = getIntent();
		final String action = intent.getAction();
		
		if (Intent.ACTION_INSERT.equals(action)) {
			mState = STATE_INSERT;
		
		} else if( Intent.ACTION_EDIT.equals(action)) {
			mState = STATE_EDIT;
			
		}
		id = intent.getLongExtra("id", 0);
		
		Log.i("filesync", "got id " + id);
		
		try {
			setContentView(R.layout.server_edit);
		} catch (Exception ex) {
			Log.e("filesync", "exception setting content view: " + ex.getMessage());
		}
		Log.i("filesync", "exiting server edit oncreate");
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		synchroSQL = new SynchroSQL(getApplicationContext());
		SQLiteDatabase db = synchroSQL.getReadableDatabase();
		String[] columns = {"server_id AS _id", "ssid", "hostname", "username", "password"};
		if (mState==STATE_INSERT){
			//ContentValues cv = new ContentValues();
			
			try {
				id = db.insert("servers", "username", null);
			} catch (Exception ex) {
				Log.e("filesync", "exception inserting: " + ex.getMessage());
			}
		}
		mCursor = db.query("servers",columns, "server_id=" + id, null, null, null, "ssid");
		
		synchroSQL = new SynchroSQL(getApplicationContext());
		
		mEditSSID = (EditText) findViewById(R.id.server_ssid);
		mEditHostname = (EditText) findViewById(R.id.server_hostname);
		mEditUsername = (EditText) findViewById(R.id.server_username);
		mEditPassword = (EditText) findViewById(R.id.server_password);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Log.i("filesync", "about to resume");
		if (mCursor != null) {
			try {
				mCursor.requery();
				
				mCursor.moveToFirst();
				
				mEditSSID.setText(mCursor.getString(mCursor.getColumnIndex("ssid")));
				mEditHostname.setText(mCursor.getString(mCursor.getColumnIndex("hostname")));
				mEditUsername.setText(mCursor.getString(mCursor.getColumnIndex("username")));
				mEditPassword.setText(mCursor.getString(mCursor.getColumnIndex("password")));
			} catch (Exception ex) {
				Log.e("filesync", "exception resuming: " + ex.getMessage());
			}
		}
		Log.i("filesync", "done with resume");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (mCursor != null) {
			if((mEditSSID.getText().length()==0) && (mState==STATE_INSERT)) {
				deleteServer();
			} else {
				saveServer();
			}
		}
	}
	
	private void saveServer() {
		try {
			ContentValues values = new ContentValues();
			values.put("ssid", mEditSSID.getText().toString());
			values.put("hostname", mEditHostname.getText().toString());
			values.put("username", mEditUsername.getText().toString());
			values.put("password", mEditPassword.getText().toString());
			
			
			SQLiteDatabase db = synchroSQL.getWritableDatabase();
			db.update("servers", values, "server_id=" + id, null);
		} catch (Exception ex) {
			Log.e("filesync", "exception saving server: " + ex.getMessage());
		}
		
	}
	
	private void deleteServer() {
		try {
			SQLiteDatabase db = synchroSQL.getWritableDatabase();
			db.delete("servers", "server_id=" + id, null);
		} catch (Exception ex) {
			Log.e("filesync", "exception deleting server: " + ex.getMessage());
		}
	}
}
