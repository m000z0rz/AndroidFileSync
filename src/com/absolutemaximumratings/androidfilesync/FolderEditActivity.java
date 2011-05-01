package com.absolutemaximumratings.androidfilesync;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class FolderEditActivity extends Activity {
	private long id;
	private Cursor mCursor;
	private SynchroSQL synchroSQL = null;
	
	private static final int STATE_INSERT = 0;
	private static final int STATE_EDIT = 1;
	
	private int mState;
	
	private Spinner mSpinnerType;
	private EditText mEditLocalPath;
	private EditText mEditRemotePath;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
			setContentView(R.layout.folder_edit);
		} catch (Exception ex) {
			Log.e("filesync", "exception setting content view: " + ex.getMessage());
		}
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		try {
			synchroSQL = new SynchroSQL(getApplicationContext());
			SQLiteDatabase db = synchroSQL.getReadableDatabase();
			String[] columns = {"folder_id AS _id", "type", "local_path", "remote_path"};
			if (mState==STATE_INSERT){
				//ContentValues cv = new ContentValues();
				
				try {
					id = db.insert("folders", "local_path", null);
				} catch (Exception ex) {
					Log.e("filesync", "exception inserting: " + ex.getMessage());
				}
			}
			mCursor = db.query("folders",columns, "folder_id=" + id, null, null, null, "local_path");
			
			synchroSQL = new SynchroSQL(getApplicationContext());
			
			mSpinnerType = (Spinner) findViewById(R.id.folder_type);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.folder_types, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mSpinnerType.setAdapter(adapter);
			mEditLocalPath = (EditText) findViewById(R.id.folder_local_path);
			mEditRemotePath = (EditText) findViewById(R.id.folder_remote_path);
		} catch (Exception ex) {
			Log.e("filesync", "error making folder edit activity: " + ex.getMessage());
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Log.i("filesync", "about to resume");
		if (mCursor != null) {
			try {
			mCursor.requery();
			
			mCursor.moveToFirst();
			mSpinnerType.setSelection(mCursor.getInt(mCursor.getColumnIndex("type"))-1);
			//mSpinnerType.setText(mCursor.getString(mCursor.getColumnIndex("type")));
			mEditLocalPath.setText(mCursor.getString(mCursor.getColumnIndex("local_path")));
			mEditRemotePath.setText(mCursor.getString(mCursor.getColumnIndex("remote_path")));
			
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
			if((mEditLocalPath.getText().length()==0) && (mState==STATE_INSERT)) {
				deleteFolder();
			} else {
				saveFolder();
			}
		}
	}
	
	private void saveFolder() {
		try {
			ContentValues values = new ContentValues();
			values.put("type", mSpinnerType.getSelectedItemPosition()+1);
			values.put("local_path", mEditLocalPath.getText().toString());
			values.put("remote_path", mEditRemotePath.getText().toString());
			
			
			SQLiteDatabase db = synchroSQL.getWritableDatabase();
			db.update("folders", values, "folder_id=" + id, null);
		} catch (Exception ex) {
			Log.e("filesync", "exception saving folder: " + ex.getMessage());
		}
		
	}
	
	private void deleteFolder() {
		try {
			SQLiteDatabase db = synchroSQL.getWritableDatabase();
			db.delete("folders", "folder_id=" + id, null);
		} catch (Exception ex) {
			Log.e("filesync", "exception deleting folder: " + ex.getMessage());
		}
	}
}
