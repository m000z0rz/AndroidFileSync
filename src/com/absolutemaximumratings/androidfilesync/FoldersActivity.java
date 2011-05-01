package com.absolutemaximumratings.androidfilesync;

import android.app.Activity;
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
import android.widget.Spinner;

public class FoldersActivity extends Activity {
	ListView foldersList = null;
	
	private SynchroSQL synchroSQL = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		Button btAdd = new Button(this);
		btAdd.setText("Add Folder");
		btAdd.setOnClickListener(btAdd_Click);
		layout.addView(btAdd);
		
		foldersList = new ListView(this);
		foldersList.setOnItemClickListener(folderItem_Click);
		registerForContextMenu(foldersList);
		layout.addView(foldersList);
		
		setContentView(layout);
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		synchroSQL = new SynchroSQL(getApplicationContext());
		
		
		//new LoadSQL().execute();
		loadSQL();
	}
	
	private OnClickListener btAdd_Click = new OnClickListener() {
		public void onClick(View v) {
			insertFolder();
			
		}
	};
	
	private void insertFolder() {
		try {
			Intent intent = null;
			intent = new Intent(FoldersActivity.this, FolderEditActivity.class);
			intent.setAction(Intent.ACTION_INSERT);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			getApplicationContext().startActivity(intent);
		} catch (Exception ex) {
			Log.e("filesync", "exception inserting folder: " + ex.getMessage());
		}
	}
	
	private void editFolder(long id) {
		try {
			Intent intent = null;
			intent = new Intent(FoldersActivity.this, FolderEditActivity.class);
			intent.setAction(Intent.ACTION_EDIT);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("id", id);
			
			getApplicationContext().startActivity(intent);
		} catch (Exception ex) {
			Log.e("filesync", "exception editing folder: " + ex.getMessage());
		}
	}
	
	private void deleteFolder(long id) {
		try {
			SQLiteDatabase db = synchroSQL.getWritableDatabase();
			db.delete("folders", "folder_id=" + id, null);
		} catch (Exception ex) {
			Log.e("filesync", "exception deleting folder: " + ex.getMessage());
		}
		SimpleCursorAdapter adapter = (SimpleCursorAdapter) foldersList.getAdapter();
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
			db = synchroSQL.getReadableDatabase();
			columns = new String[] {"folder_id AS _id", "local_path", "remote_path"};
			cursor = db.query("folders",columns, "", null, null, null, "local_path");
			String[] from = {"local_path", "remote_path"};
			int[] to = { R.id.item_title, R.id.item_subtitle };
			SimpleCursorAdapter folderAdapt = new SimpleCursorAdapter(this, R.layout.item_entry, cursor, from, to);
			foldersList.setAdapter(folderAdapt);
			//serversList.setOnItemClickListener(serverItem_Click);
			
		} catch (Exception ex) {
			Log.e("filesync", "loadsql exception: " + ex.getMessage());
		}
	}
	
	OnItemClickListener folderItem_Click = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.i("filesync", "into listener");
			
			editFolder(id);
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
			editFolder(info.id);
			return true;
		case R.id.menu_delete:
			deleteFolder(info.id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
}
