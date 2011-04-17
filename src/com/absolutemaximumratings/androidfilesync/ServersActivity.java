package com.absolutemaximumratings.androidfilesync;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ServersActivity extends Activity {
	ListView serversList = null;
	
	private SynchroSQL synchroSQL = null;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("filesync", "servers activity on create");
		
		serversList = new ListView(this);
		serversList.setOnItemClickListener(serverItem_Click);
		setContentView(serversList);
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i("filesync", "onstart");
		synchroSQL = new SynchroSQL(getApplicationContext());
		
		
		//new LoadSQL().execute();
		loadSQL();
		Log.i("filesync", "post async execute");
	}
	
	private void makeTestData(String ssid, String hostname) {
		try {
		SQLiteDatabase db = synchroSQL.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("ssid", ssid);
		cv.put("hostname", hostname);
		db.insert("servers", null, cv);
		} catch (Exception ex) {
			Log.e("filesync","make: " + ex.getMessage());
		}
		
	}
	
	private void loadSQL() {
		Log.i("filesync", "start sql");
		SQLiteDatabase db = null;
		Cursor cursor = null;
		String[] columns = null;
		
		try {
			//makeTestData("fof","192.168.1.2");
			db = synchroSQL.getReadableDatabase();
			columns = new String[] {"server_id AS _id", "ssid", "hostname"};
			cursor = db.query("servers",columns, "", null, null, null, "ssid");
			String[] from = {"ssid", "hostname"};
			int[] to = { R.id.item_title, R.id.item_subtitle };
			SimpleCursorAdapter serverAdapt = new SimpleCursorAdapter(this, R.layout.item_entry, cursor, from, to);
			serversList.setAdapter(serverAdapt);
			//serversList.setOnItemClickListener(serverItem_Click);
			
		} catch (Exception ex) {
			Log.e("filesync", "loadsql exception: " + ex.getMessage());
		}
	}
	
	
	OnItemClickListener serverItem_Click = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(), "hello id " + id, Toast.LENGTH_SHORT).show();
			
			Intent intent = null;
			try {
				intent = new Intent(ServersActivity.this, ServerEditActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			} catch (Exception ex3) {
				Log.e("filesync", "on intent ex3: " + ex3.getMessage());
			}
			
			try {
	            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getApplicationContext().startActivity(intent);
			
	            //startActivity(intent);
			} catch (Exception ex2) {
				Log.e("filesync", "onitemclick ex2: " + ex2.getMessage() + " blah " + ex2.getStackTrace());
			}
		}
	};
}
