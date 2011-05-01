package com.absolutemaximumratings.androidfilesync;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ServersActivity extends Activity {
	ListView serversList = null;
	
	private SynchroSQL synchroSQL = null;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("filesync", "servers activity on create");
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		Button btAdd = new Button(this);
		btAdd.setText("Add Server");
		btAdd.setOnClickListener(btAdd_Click);
		layout.addView(btAdd);
		
		serversList = new ListView(this);
		serversList.setOnItemClickListener(serverItem_Click);
		
		registerForContextMenu(serversList);
		
		layout.addView(serversList);
		
		
		setContentView(layout);
		
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
	
	private OnClickListener btAdd_Click = new OnClickListener() {
		public void onClick(View v) {
			insertServer();
			
		}
	};
	
	private void insertServer() {
		try {
			Intent intent = null;
			intent = new Intent(ServersActivity.this, ServerEditActivity.class);
			intent.setAction(Intent.ACTION_INSERT);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			getApplicationContext().startActivity(intent);
		} catch (Exception ex) {
			Log.e("filesync", "exception inserting server: " + ex.getMessage());
		}
	}
	
	private void editServer(long id) {
		try {
			Intent intent = null;
			intent = new Intent(ServersActivity.this, ServerEditActivity.class);
			intent.setAction(Intent.ACTION_EDIT);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("id", id);
			
			getApplicationContext().startActivity(intent);
		} catch (Exception ex) {
			Log.e("filesync", "exception editing server: " + ex.getMessage());
		}
	}
	
	private void deleteServer(long id) {
		try {
			SQLiteDatabase db = synchroSQL.getWritableDatabase();
			db.delete("servers", "server_id=" + id, null);
		} catch (Exception ex) {
			Log.e("filesync", "exception deleting server: " + ex.getMessage());
		}
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) serversList.getAdapter();
		adapter.getCursor().requery();
		adapter.notifyDataSetChanged();

		Log.i("filesync", "adapter notified2");
		
	}
	
	private void loadSQL() {
		Log.i("filesync", "start sql");
		SQLiteDatabase db = null;
		Cursor cursor = null;
		String[] columns = null;
		
		try {
			//makeTestData("fof2","192.168.1.3");
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
			Log.i("filesync", "into listener");
			
			editServer(id);
		}
	};
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.basic_context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.menu_edit:
			editServer(info.id);
			return true;
		case R.id.menu_delete:
			deleteServer(info.id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
}
