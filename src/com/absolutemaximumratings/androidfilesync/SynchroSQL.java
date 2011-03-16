package com.absolutemaximumratings.androidfilesync;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SynchroSQL extends SQLiteOpenHelper {
	private static final String dbName = "androidFileSync.db";
	private static final int dbVersion = 1;
	
	SynchroSQL(Context context) {
		super(context, dbName, null, dbVersion);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE servers ("
				+ "server_id INTEGER PRIMARY KEY, "
				+ "ssid TEXT, "
				+ "hostname TEXT, "
				+ "username TEXT, "
				+ "password TEXT"
				+ ");"
				);
		
		db.execSQL(
				"CREATE TABLE folders ("
				+ "folder_id INTEGER PRIMARY KEY, "
				+ "type INTEGER, " // 1-push 2-pull 3-both
				+ "local_path TEXT, "
				+ "remote_path TEXT"
				+ ");"
				);
		
		db.execSQL(
				"CREATE TABLE synchronizations ("
				+ "server_id INTEGER, "
				+ "folder_id INTEGER"
				+ ");"
				);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w("filesync", "Upgrade database...?!");
		
		// db.execSQL("DROP TABLE IF EXISTS servers");
		// onCreate(db);
		
	}
	
}
