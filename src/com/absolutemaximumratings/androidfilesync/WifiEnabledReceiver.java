package com.absolutemaximumratings.androidfilesync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiEnabledReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		NetworkInfo netInfo = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		
		if(netInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
			//could also check wifi stuff?
			Intent synchroIntent = new Intent(context, Synchronizer.class);
			context.startService(synchroIntent);
		}
	}
	
	private void oldOnReceive(Context context, Intent intent) {
		//Toast.makeText(context, "onReceive", Toast.LENGTH_SHORT).show();

		//WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		//WifiInfo wifiInfo = wifi.getConnectionInfo();
		
		Log.i("filesync", "6onReceive Begin");
		Log.i("filesync", "action name: " + intent.getAction());
		
		NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		Log.i("filesync", "state: " + info.getState());
		if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
			Log.i("filesync", "connected");
		} else if (info.getState().equals(NetworkInfo.State.CONNECTING)) {
			Log.i("filesync", "connecting");
		}
		//Log.i("filesync", "tostring: " + wifi.toString());
		//Log.i("filesync", "state: " + wifi.getWifiState());
		//Log.i("filesync", "bssid: " + wifiInfo.getBSSID());
		//Log.i("filesync", "ssid: " + wifiInfo.getSSID());
		//Log.i("filesync", "supplicant state: " + wifiInfo.getSupplicantState());
		//Log.i("filesync", "describe: " + wifiInfo.describeContents());
		try {
			WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifi.getConnectionInfo();
			Log.i("filesync", "bssid: " + wifiInfo.getBSSID());
			Log.i("filesync", "ssid: " + wifiInfo.getSSID());
			
			Intent newIntent = new Intent(context, Synchronizer.class);
			context.startService(newIntent);
			Log.i("filesync", "afters...");
		} catch (Exception ex) {
			Log.e("filesync", ex.getMessage());
		}
		Log.i("filesync", "onReceive End");
	}

}
