package com.absolutemaximumratings.androidfilesync;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FoldersActivity extends Activity {
	ListView foldersList = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		foldersList = new ListView(this);
		foldersList.setOnItemClickListener(folderItem_Click);
		setContentView(foldersList);
		
		
		//TextView textview = new TextView(this);
		//textview.setText("This is the folders tab");
		//setContentView(textview);
	}
	
	OnItemClickListener folderItem_Click = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			
		}
	};
	
}
