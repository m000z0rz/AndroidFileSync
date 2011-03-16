package com.absolutemaximumratings.androidfilesync;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ServersActivity extends Activity {
	ListView serversList = null;
	
	private SynchroSQL synchroSQL = null;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("filesync", "servers activity on create");
		
		serversList = new ListView(this);
		serversList.setOnItemClickListener(serverItem_Click);
		setContentView(serversList);
		
		//TextView textview = new TextView(this);
		//textview.setText("This is the Servers tab");
		//setContentView(textview);
	}

	private class LoadSQL extends AsyncTask<Void, Void, Cursor> {

		@Override
		protected Cursor doInBackground(Void... arg0) {

			Log.i("filesync", "doinbackground");
			SQLiteDatabase db = null;
			Cursor result = null;
			Log.i("filesync", "1");
			ContentValues values = new ContentValues();
			String[] columns = null;
			
			Log.i("filesync", "2");
			try {
				//synchroSQL = new SynchroSQL(getApplicationContext());
				if(synchroSQL==null) Log.e("filesync", "synchrosql null");
				db = synchroSQL.getReadableDatabase();
				Log.i("filesync", "3");
				
				columns = new String[] {"server_id AS _id", "ssid", "hostname"};
				values.put("ssid", DateFormat.getDateInstance().format(new Date()));
				values.put("hostname", "nabisco");
				Log.i("filesync", "4");
			} catch (Exception ex) {
				Log.e("filesync", "loadsql ex1: " + ex.getMessage());
			}
			
			
			
			Log.i("filesync", "5");
			try {
				//db.insert("servers", null, values);
				Log.i("filesync", "6");
				result = db.query("servers", columns, "", null, null, null, "ssid");
				Log.i("filesync", "7");
			} catch (Exception ex) {
				Log.e("filesync", "loadsql exception: " + ex.getMessage());
			}
			Log.i("filesync", "just before return...");
			return result;
			//return result;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			Context context = getApplicationContext();
			Toast.makeText(context, "post", Toast.LENGTH_SHORT);
			Log.i("filesync", "on post execute");
			//TextView tvTitle;
			//TextView tvSubtitle;
			//View child;

			//String title;
			//String subtitle;
			try {
			
				//startManagingCursor(result);
				Log.i("filesync", "results: " + result.getCount());
				String[] from = new String[] {"ssid", "hostname"};
				int[] to = new int[] { R.id.item_title, R.id.item_subtitle };
				
				SimpleCursorAdapter serverAdapt = new SimpleCursorAdapter(context, R.layout.item_entry, result, from, to);
				
				
				/*
				
				
				while(!result.isLast()) {
					result.moveToNext();
					title = result.getString(result.getColumnIndex("ssid"));
					subtitle = result.getString(result.getColumnIndex("hostname"));
					
					Log.i("filesync", "title: " + title + "    subtitle: " + subtitle);
					
					child = View.inflate(context, R.layout.item_entry, null);
					Log.i("filesync", "inflated");
					tvTitle = (TextView) child.findViewById(R.id.item_title);
					tvSubtitle = (TextView) child.findViewById(R.id.item_subtitle);
					
					tvTitle.setText(title);
					tvSubtitle.setText(subtitle);
					
					serversList.addView(child);
				}
				*/
			} catch (Exception ex) {
				Log.e("filesync", "exceptions!!! " + ex.getMessage());
			}
			Log.i("filesync", "closing post execute");
		}
		
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
	
	private void loadSQL() {
		Log.i("filesync", "start sql");
		SQLiteDatabase db = null;
		Cursor cursor = null;
		String[] columns = null;
		
		try {
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
		}
	};
}
