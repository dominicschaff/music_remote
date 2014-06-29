package com.dominic.remote;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;
import android.view.KeyEvent;

public class Actions {


	private final int NONE = 0, SINGLE = 1, VOLUME = 3, TRACKS = 2,
			PLAYBACK = 4;

	private final String[][] ACTIONS = new String[][] {
			new String[] { "", "", "" }, new String[] { "", "", "" },
			new String[] { "Previous", "No Change", "Skip" },
			new String[] { "Lower", "No Change", "Raise" },
			new String[] { "Stop", "No Change", "Play/Pause" } };
	
	private Context context;

	public Actions(Context context)
	{
		this.context = context;
	}

	/**
	 * This is to print out the action.
	 * 
	 * @return Which action must run.
	 */
	public String getAction(int action) {
		switch (action) {
		case NONE:
			return "None";
		case SINGLE:
			return "Add More Fingers";
		case VOLUME:
			return "Volume";
		case TRACKS:
			return "Track";
		case PLAYBACK:
			return "Playback";
		default:
			return "Try other fingers";

		}
	}

	/**
	 * Get the text description of the direction
	 * 
	 * @return
	 */
	public String getDirectionString(int action, int direction) {
		return ACTIONS[action][direction];
	}

	/**
	 * Run the current action.
	 */
	public void runAction(int action, int direction) {
		// if in dead zone do nothing.
		if (direction == 1) {
			return;
		}

		// this is for whether key operations should get done.
		int event = 0;
		boolean change = false;

		// What Action?
		switch (action) {
		case SINGLE:
			break;
		case VOLUME:
			AudioManager audioManager = (AudioManager) this.context
					.getSystemService(Context.AUDIO_SERVICE);
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					direction - 1, 0);
			return;
		case TRACKS:
			event = direction > 1 ? KeyEvent.KEYCODE_MEDIA_NEXT
					: KeyEvent.KEYCODE_MEDIA_PREVIOUS;
			change = true;
			break;
		case PLAYBACK:
			event = direction > 1 ? KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
					: KeyEvent.KEYCODE_MEDIA_STOP;
			change = true;
			break;
		case NONE:
		default:
			return;
		}

		// Should I send a key event?
		if (change) {
			// Press The Button
			long eventtime = SystemClock.uptimeMillis();
			Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
			KeyEvent downEvent = new KeyEvent(eventtime, eventtime,
					KeyEvent.ACTION_DOWN, event, 0);
			downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
			this.context.sendOrderedBroadcast(downIntent, null);

			// Release The Button
			Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
			KeyEvent upEvent = new KeyEvent(eventtime, eventtime,
					KeyEvent.ACTION_UP, event, 0);
			upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
			this.context.sendOrderedBroadcast(upIntent, null);
		}
	}
}
