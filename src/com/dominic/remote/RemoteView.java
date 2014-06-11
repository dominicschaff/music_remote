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

public class RemoteView extends View {

	// drawing and canvas paint
	private Paint drawPaint, canvasPaint, textPaint, drawRed, textRight;
	// initial color
	private final int paintColor = 0xFFFFFFFF;
	// canvas bitmap
	private Bitmap canvasBitmap;

	private float centerX = 0;
	private float centerY = 0;
	private float radius = 0;
	private int width, height, diffs;
	private String track = "", artist = "", album = "";
	private int BatteryLevel = -1;
	private int countdown = 100;
	private final int countdowndefault = 100;

	public RemoteView(Context context, AttributeSet attrs) {
		super(context, attrs);
		doSetup();
	}

	private void doSetup() {
		drawPaint = new Paint();
		drawPaint.setColor(paintColor);
		drawPaint.setAntiAlias(true);
		drawPaint.setStrokeWidth(10);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);
		drawRed = new Paint();
		drawRed.setColor(0xFFFF4400);
		drawRed.setAntiAlias(true);
		drawRed.setStrokeWidth(10);
		drawRed.setStyle(Paint.Style.FILL_AND_STROKE);
		textPaint = new Paint();
		textPaint.setColor(paintColor);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(48);
		textRight = new Paint();
		textRight.setColor(paintColor);
		textRight.setAntiAlias(true);
		textRight.setTextSize(48);
		textRight.setTextAlign(Paint.Align.RIGHT);
		canvasPaint = new Paint(Paint.DITHER_FLAG);
		Display display = ((Activity) this.getContext()).getWindowManager()
				.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;
		diffs = height / 20;

		IntentFilter iF = new IntentFilter();
		iF.addAction("com.android.music.metachanged");
		iF.addAction("com.android.music.playstatechanged");
		iF.addAction("com.android.music.playbackcomplete");
		iF.addAction("com.android.music.queuechanged");

		this.getContext().registerReceiver(mReceiver, iF);
		updateBattery();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		countdown--;
		if (countdown < 0) {
			updateBattery();
			countdown = countdowndefault;
		}
		// draw view
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		if (startY != 0)
			canvas.drawRect(0, startY - diffs, width, startY + diffs, drawRed);
		canvas.drawCircle(centerX, centerY, radius, drawPaint);

		canvas.drawText(track, 0, 48, textPaint);

		canvas.drawText(artist, 0, 2 * 48, textPaint);

		canvas.drawText(album, 0, 3 * 48, textPaint);

		if (currentPoints > 1)
			canvas.drawText("Action: " + getAction(currentPoints) + " "
					+ getDirectionString(), 0, 4 * 48, textPaint);
		Calendar c = Calendar.getInstance();

		canvas.drawText(BatteryLevel + " %", width, 2 * 48, textRight);
		canvas.drawText(
				c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE),
				width, 48, textRight);
	}

	private float lx, rx, ly, ry, x, y;
	private int currentPoints = 0;
	private boolean getX = false, setRadius = false;
	private float startX = 0, startY = 0;

	public boolean onTouchEvent(MotionEvent event) {
		int points = event.getPointerCount();
		if (event.getAction() == MotionEvent.ACTION_UP) {
			runAction(currentPoints, getDirection());
			rx = ry = lx = ly = centerX = centerY = radius = startX = startY = currentPoints = 0;
			return true;
		}
		if (currentPoints == 0 || currentPoints < points) {
			setRadius = true;
			currentPoints = points;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			getX = true;
		case MotionEvent.ACTION_MOVE:
			if (!getX || points < 2) {
				break;
			}
			lx = rx = event.getX(0);
			for (int i = 0; i < points; i++) {
				x = event.getX(i);
				if (x < lx) {
					lx = x;
				}
				if (x > rx) {
					rx = x;
				}
			}
			centerX = (float) (rx - lx) / 2 + lx;
			getX = false;
			break;
		case MotionEvent.ACTION_UP:
			rx = ry = lx = ly = centerX = centerY = 0;
			break;
		default:
			return false;
		}

		ly = ry = event.getY(0);
		for (int i = 0; i < points; i++) {
			y = event.getY(i);
			if (y < ly) {
				ly = y;
			}
			if (y > ry) {
				ry = y;
			}
		}
		centerY = (float) (ry - ly) / 2 + ly;
		if (setRadius) {
			radius = (float) Math.sqrt((ry - ly) * (ry - ly) + (rx - lx)
					* (rx - lx)) / 2;
			setRadius = false;
		}
		if (startX == 0 && startY == 0) {
			startX = centerX;
			startY = centerY;
		}
		invalidate();
		return true;
	}

	private String getAction(int action) {
		switch (action) {
		case 0:
			return "None";
		case 1:
			return "Same";
		case 2:
			return "Change Volume";
		case 3:
			return "Skip Tracks";
		default:
			return "Try other fingers";

		}
	}

	private int getDirection() {
		float diff = startY - centerY;
		if (diff <= diffs && diff >= -diffs)
			return 0;
		return startY > centerY ? 1 : -1;
	}

	private String getDirectionString() {
		float diff = startY - centerY;
		if (diff <= diffs && diff >= -diffs)
			return "None";
		return startY > centerY ? "Up" : "Down";
	}

	private void runAction(int action, int direction) {
		if (direction == 0) {
			return;
		}
		// AudioManager mAudioManager = (AudioManager)
		// host.getSystemService(Context.AUDIO_SERVICE);
		//
		// if (mAudioManager.isMusicActive()) {
		// System.out.println("ACTIVE");
		// Intent i = new Intent(SERVICECMD);
		// i.putExtra(CMDNAME, CMDNEXT);
		// host.sendBroadcast(i);
		// }
		int event = 0;
		boolean change = false;
		switch (action) {
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

		if (change) {
			long eventtime = SystemClock.uptimeMillis();
			Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
			KeyEvent downEvent = new KeyEvent(eventtime, eventtime,
					KeyEvent.ACTION_DOWN, event, 0);
			downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
			this.getContext().sendOrderedBroadcast(downIntent, null);

			Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
			KeyEvent upEvent = new KeyEvent(eventtime, eventtime,
					KeyEvent.ACTION_UP, event, 0);
			upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
			this.getContext().sendOrderedBroadcast(upIntent, null);
		}
	}

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

	private void updateBattery() {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = this.getContext()
				.registerReceiver(null, ifilter);
		// // Are we charging / charged?
		// int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS,
		// -1);
		// boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
		// ||
		// status == BatteryManager.BATTERY_STATUS_FULL;
		//
		// // How are we charging?
		// int chargePlug =
		// batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		// boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		// boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

		BatteryLevel = batteryStatus
				.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	}
}
