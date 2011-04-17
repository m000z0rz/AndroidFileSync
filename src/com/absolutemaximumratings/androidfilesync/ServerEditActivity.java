package com.absolutemaximumratings.androidfilesync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ServerEditActivity extends Activity {
	long server_id;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//final Intent intent = getIntent();
		//server_id = intent.getLongExtra("server_id", 0);
		setContentView(R.layout.server_edit);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// TODO: save here
	}
}
