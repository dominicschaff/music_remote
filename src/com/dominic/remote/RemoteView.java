package com.dominic.remote;

import java.util.Calendar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * This is the monster class that does everything.
 * 
 * Note that currently I have a problem and am leaking a IntentRecevier, I am
 * still working on how to fix this.
 * 
 * @author Dominic Schaff
 * 
 */
public class RemoteView extends View {

	// drawing and canvas paint
	private Paint circlePaint, canvasPaint, textPaint, deadzonePaine,
			textRightPaint;

	// Colours
	private final int WHITE = 0xFFFFFFFF, RED = 0xFFFF0000;

	// canvas bitmap
	private Bitmap canvasBitmap;

	// Screen information
	private int width, height, diffs, rotation;

	// Media Information
	private String track = "", artist = "", album = "";

	// Battery Status checks
	private String BatteryLevel = "";
	private final int COUNTDOWN_DEFAULT = 100;
	private int countdown = COUNTDOWN_DEFAULT;

	// Finger Information
	private float centerX = 0;
	private float centerY = 0;
	private float radius = 0;
	private int currentAction = 0;
	private boolean getX = false, setRadius = false;
	private float startX = 0, startY = 0;

	public RemoteView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Initialize all the paint styles

		circlePaint = new Paint();
		circlePaint.setColor(WHITE);
		circlePaint.setAntiAlias(true);
		circlePaint.setStrokeWidth(10);
		circlePaint.setStyle(Paint.Style.STROKE);
		circlePaint.setStrokeJoin(Paint.Join.ROUND);
		circlePaint.setStrokeCap(Paint.Cap.ROUND);
		deadzonePaine = new Paint();
		deadzonePaine.setColor(RED);
		deadzonePaine.setAntiAlias(true);
		deadzonePaine.setStrokeWidth(10);
		deadzonePaine.setStyle(Paint.Style.FILL_AND_STROKE);
		textPaint = new Paint();
		textPaint.setColor(WHITE);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(48);
		textRightPaint = new Paint();
		textRightPaint.setColor(WHITE);
		textRightPaint.setAntiAlias(true);
		textRightPaint.setTextSize(48);
		textRightPaint.setTextAlign(Paint.Align.RIGHT);
		canvasPaint = new Paint(Paint.DITHER_FLAG);

		// Get Screen Information
		Display display = ((Activity) this.getContext()).getWindowManager()
				.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;
		diffs = height / 20;
		rotation = display.getRotation();

		// Register to hear about media changes
		IntentFilter iF = new IntentFilter();
		iF.addAction("com.android.music.metachanged");
		iF.addAction("com.android.music.playstatechanged");
		iF.addAction("com.android.music.playbackcomplete");
		iF.addAction("com.android.music.queuechanged");
		getContext().registerReceiver(mReceiver, iF);

		// Get updated battery information
		updateBattery();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Check if battery should be updated
		countdown--;
		if (countdown < 0) {
			updateBattery();
			countdown = COUNTDOWN_DEFAULT;
		}
		// Background
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);

		if ((rotation & 1) == 0) {
			// draw vertical
			// Dead Zone
			if (startY != 0)
				canvas.drawRect(0, startY - diffs, width, startY + diffs,
						deadzonePaine);

			// Finger Location
			canvas.drawCircle(centerX, centerY, radius, circlePaint);
		} else {
			// draw horizontal
			// Dead Zone
			if (startY != 0)
				canvas.drawRect(startY - diffs, 0, startY + diffs, height,
						deadzonePaine);

			// Finger Location
			canvas.drawCircle(centerY, centerX, radius, circlePaint);
		}

		// Media Information
		canvas.drawText(track, 0, 48, textPaint);
		canvas.drawText(artist, 0, 2 * 48, textPaint);
		canvas.drawText(album, 0, 3 * 48, textPaint);

		// Update Action Information
		if (currentAction > 1)
			canvas.drawText("Action: " + getAction() + " "
					+ getDirectionString(), 0, 4 * 48, textPaint);

		// Get Time and Battery
		Calendar c = Calendar.getInstance();
		canvas.drawText(BatteryLevel, width, 2 * 48, textRightPaint);
		canvas.drawText(
				c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE),
				width, 48, textRightPaint);
	}

	public boolean onTouchEvent(MotionEvent event) {
		// How many fingers?
		int points = event.getPointerCount();

		// Did you remove your fingers?
		if (event.getAction() == MotionEvent.ACTION_UP) {
			runAction();
			centerX = centerY = radius = startX = startY = currentAction = 0;
			return true;
		}

		// Are you adding fingers?
		if (currentAction == 0 || currentAction < points) {
			setRadius = true;
			currentAction = points;
		}
		float[][] fingers = new float[2][points];
		for (int i = 0; i < points; i++) {
			fingers[1][i] = event.getY(i);
			fingers[0][i] = event.getX(i);
		}
		float[][] maxes = new float[2][3];

		// What event just happened?
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			getX = true;
		case MotionEvent.ACTION_MOVE:
			// You must have at least 2 fingers.
			if (!getX || points < 2) {
				break;
			}
			// Get x position of finger
			maxes[0] = getDiffs(fingers[this.rotation % 2]);
			centerX = (float) (maxes[0][2]) / 2 + maxes[0][0];
			getX = false;
			break;
		case MotionEvent.ACTION_UP:
			break;
		default:
			return false;
		}

		// Get Y position
		maxes[1] = getDiffs(fingers[(this.rotation + 1) % 2]);
		centerY = (float) (maxes[1][2]) / 2 + maxes[1][0];

		// Should I re work out the radius?
		if (setRadius) {
			radius = (float) Math.sqrt((maxes[1][2]) * (maxes[1][2])
					+ (maxes[0][2]) * (maxes[0][2])) / 2;
			setRadius = false;
		}

		// is this the first time I added fingers.
		if (startX == 0 && startY == 0 && centerX > 0 && centerY > 0) {
			startX = centerX;
			startY = centerY;
		}

		// make the screen redraw
		invalidate();
		return true;
	}

	private float[] getDiffs(float[] points) {
		float diffs[] = new float[3];
		diffs[0] = points[0];
		diffs[1] = points[0];
		for (float f : points) {
			if (f > diffs[1]) {
				diffs[1] = f;
			}
			if (f < diffs[0]) {
				diffs[0] = f;
			}
		}
		diffs[2] = diffs[1] - diffs[0];
		return diffs;
	}

	/**
	 * This is to print out the action.
	 * 
	 * @return Which action must run.
	 */
	private String getAction() {
		switch (currentAction) {
		case 0:
			return "None";
		case 1:
			return "Add More Fingers";
		case 2:
			return "Change Volume";
		case 3:
			return "Skip Tracks";
		default:
			return "Try other fingers";

		}
	}

	/**
	 * Get the direction (up or down).
	 * 
	 * @return The direction.
	 */
	private int getDirection() {
		float diff = startY - centerY;
		if (diff <= diffs && diff >= -diffs)
			return 0;

		if ((rotation & 1) == 0) {
			return startY > centerY ? 1 : -1;
		} else {
			return startY > centerY ? -1 : 1;
		}
	}

	/**
	 * Get the text description of the direction
	 * 
	 * @return
	 */
	private String getDirectionString() {
		switch (getDirection()) {
		case -1:
			return "Down";
		case 1:
			return "Up";
		case 0:
		default:
			return "None";
		}
	}

	/**
	 * Run the current action.
	 */
	private void runAction() {
		int direction = getDirection();
		// if in dead zone do nothing.
		if (direction == 0) {
			return;
		}

		// this is for whether key operations should get done.
		int event = 0;
		boolean change = false;

		// What Action?
		switch (currentAction) {
		case 1:
			break;
		case 2:
			AudioManager audioManager = (AudioManager) this.getContext()
					.getSystemService(Context.AUDIO_SERVICE);
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					direction, 0);
			return;
		case 3:
			event = direction > 0 ? KeyEvent.KEYCODE_MEDIA_NEXT
					: KeyEvent.KEYCODE_MEDIA_PREVIOUS;
			change = true;
			break;
		case 0:
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
			this.getContext().sendOrderedBroadcast(downIntent, null);

			// Release The Button
			Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
			KeyEvent upEvent = new KeyEvent(eventtime, eventtime,
					KeyEvent.ACTION_UP, event, 0);
			upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
			this.getContext().sendOrderedBroadcast(upIntent, null);
		}
	}

	/**
	 * This does receives the broadcast for media changes.
	 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// String action = intent.getAction();
			// String cmd = intent.getStringExtra("command");
			artist = intent.getStringExtra("artist") == null ? "" : intent
					.getStringExtra("artist");
			album = intent.getStringExtra("album") == null ? "" : intent
					.getStringExtra("album");
			track = intent.getStringExtra("track") == null ? "" : intent
					.getStringExtra("track");

			invalidate();
		}
	};

	/**
	 * Update the battery information.
	 */
	private void updateBattery() {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = this.getContext()
				.registerReceiver(null, ifilter);

		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
				|| status == BatteryManager.BATTERY_STATUS_FULL;

		BatteryLevel = batteryStatus
				.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
				+ " %"
				+ (isCharging ? "+" : "");
	}
}
