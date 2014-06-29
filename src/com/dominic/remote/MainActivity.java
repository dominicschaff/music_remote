package com.dominic.remote;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;

/**
 * This is the main activity it runs all the other things.
 * 
 * @author Dominic Schaff
 * 
 */
public class MainActivity extends Activity {

	RemoteView rv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		rv = (RemoteView) this.findViewById(R.id.drawing);
		rv.setActions(new Actions(this));

		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent batteryStatus) {
				int status = batteryStatus.getIntExtra(
						BatteryManager.EXTRA_STATUS, -1);
				boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
						|| status == BatteryManager.BATTERY_STATUS_FULL;

				int level = batteryStatus.getIntExtra(
						BatteryManager.EXTRA_LEVEL, -1);
				rv.setBatteryValue(level, isCharging);
			}

		}, ifilter);

		IntentFilter iF = new IntentFilter();
		iF.addAction("com.android.music.metachanged");
		iF.addAction("com.android.music.playstatechanged");
		iF.addAction("com.android.music.playbackcomplete");
		iF.addAction("com.android.music.queuechanged");
		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// String action = intent.getAction();
				// String cmd = intent.getStringExtra("command");
				String artist = intent.getStringExtra("artist") == null ? ""
						: intent.getStringExtra("artist");
				String album = intent.getStringExtra("album") == null ? ""
						: intent.getStringExtra("album");
				String title = intent.getStringExtra("track") == null ? ""
						: intent.getStringExtra("track");
				rv.setSongInformation(title, artist, album);
			}
		}, iF);

	}

	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		} else {
			unregisterReceiver(null);
		}
	}
}
