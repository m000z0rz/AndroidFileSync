package com.absolutemaximumratings.androidfilesync;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ServerEditActivity extends Activity {
	
	private long id;
	private Cursor mCursor;
	private SynchroSQL synchroSQL = null;
	
	private static final int STATE_INSERT = 0;
	private static final int STATE_EDIT = 1;
	
	private int mState;
	
	private LinearLayout mLayout;
	private EditText mEditSSID;
	private EditText mEditHostname;
	private EditText mEditUsername;
	private EditText mEditPassword;
	
	ArrayList<CheckBox> checkBoxes;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i("filesync", "into server edit oncreate");
		
		checkBoxes = new ArrayList<CheckBox>();
		
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
		//Log.i("filesync", "exiting server edit oncreate");
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		synchroSQL = new SynchroSQL(getApplicationContext());
		SQLiteDatabase db = synchroSQL.getReadableDatabase();
		String[] columns = {"server_id AS _id", "ssid", "hostname", "username", "password"};
		if (mState==STATE_INSERT){
			try {
				id = db.insert("servers", "username", null);
			} catch (Exception ex) {
				Log.e("filesync", "exception inserting: " + ex.getMessage());
			}
		}
		mCursor = db.query("servers",columns, "server_id=" + id, null, null, null, "ssid");
		
		//synchroSQL = new SynchroSQL(getApplicationContext());
		
		mLayout = (LinearLayout) findViewById(R.id.server_edit_layout);
		mEditSSID = (EditText) findViewById(R.id.server_ssid);
		mEditHostname = (EditText) findViewById(R.id.server_hostname);
		mEditUsername = (EditText) findViewById(R.id.server_username);
		mEditPassword = (EditText) findViewById(R.id.server_password);
		
		try {
			String[] folderColumns = {"folder_id", "type", "local_path", "remote_path"};
			Cursor foldersCursor = db.query("folders", folderColumns, null, null, null, null, "local_path");
			
			foldersCursor.moveToFirst();
			while(!foldersCursor.isAfterLast()) {
				CheckBox folderBox = new CheckBox(this);
				int type;
				
				
				folderBox.setTag(new Integer(foldersCursor.getInt(foldersCursor.getColumnIndex("folder_id"))));
				String displayText = "";
				type = foldersCursor.getInt(foldersCursor.getColumnIndex("type"));
				if (type==1) displayText += "(Push)";
				else if (type==2) displayText += "(Pull)";
				else if (type==3) displayText += "(Both)";
				displayText += " " + foldersCursor.getString(foldersCursor.getColumnIndex("local_path"));
				displayText += " / " + foldersCursor.getString(foldersCursor.getColumnIndex("remote_path"));
				folderBox.setText(displayText);
				mLayout.addView(folderBox);
				
				checkBoxes.add(folderBox);
				
				foldersCursor.moveToNext();
			}
		} catch (Exception ex) {
			Log.e("filesync", "checkbox making: " + ex.getMessage());
		}
		
		//checkbox
		mCursor = db.query("servers",columns, "server_id=" + id, null, null, null, "ssid");
		
	}
	
	private void setCheckBoxes() {
		CheckBox curBox;
		int folder_id;
		SQLiteDatabase db = synchroSQL.getReadableDatabase();
		String[] columns = {"folder_id"};
		Cursor cursor = db.query("synchronizations", columns, "server_id=" + id, null, null, null, null);
		
		cursor.moveToFirst();
		try {
			while(!cursor.isAfterLast()) {
				folder_id = cursor.getInt(cursor.getColumnIndex("folder_id"));
				Log.i("filesync", "found synchro to folder with id " + folder_id);
				curBox = (CheckBox) mLayout.findViewWithTag(new Integer(folder_id));
				curBox.setChecked(true);
				
				cursor.moveToNext();
			}
		} catch (Exception ex) {
			Log.e("filesync", "setCheckBoxes() exception: " + ex.getMessage());
		}
	}
	
	private void saveCheckBoxes() {
		CheckBox curBox;
		Iterator<CheckBox> it = checkBoxes.iterator();
		SQLiteDatabase db = synchroSQL.getWritableDatabase();
		
		db.delete("synchronizations", "server_id=" + id, null);
		
		while (it.hasNext()) {
			curBox = it.next();
			if(curBox.isChecked()) {
				ContentValues cv = new ContentValues();
				cv.put("folder_id", ((Integer) curBox.getTag()).intValue());
				cv.put("server_id", id);
				db.insert("synchronizations",null, cv);
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//Log.i("filesync", "about to resume");
		if (mCursor != null) {
			try {
				mCursor.requery();
				
				mCursor.moveToFirst();
				
				mEditSSID.setText(mCursor.getString(mCursor.getColumnIndex("ssid")));
				mEditHostname.setText(mCursor.getString(mCursor.getColumnIndex("hostname")));
				mEditUsername.setText(mCursor.getString(mCursor.getColumnIndex("username")));
				mEditPassword.setText(mCursor.getString(mCursor.getColumnIndex("password")));
				
				setCheckBoxes();
			} catch (Exception ex) {
				Log.e("filesync", "exception resuming: " + ex.getMessage());
			}
		}
		//Log.i("filesync", "done with resume");
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
			
			saveCheckBoxes();
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
