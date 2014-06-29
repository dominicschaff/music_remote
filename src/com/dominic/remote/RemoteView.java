package com.dominic.remote;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
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
	private final int WHITE = 0xFFFFFFFF, RED = 0xFFFF0000, GREEN = 0xFF00FF00,
			LIGHT_GREEN = 0xFF00FFAA;

	// canvas bitmap
	private Bitmap canvasBitmap;

	// Screen information
	private int width, height, diffs, rotation, textSize, textSize2, textSize3,
			textSize4;

	// Media Information
	private String title = "", artist = "", album = "";

	// Battery Status checks
	private String BatteryLevel = "";

	// Finger Information
	private float centerX = 0;
	private float centerY = 0;
	private float radius = 0;
	private int currentAction = 0;
	private boolean getX = false, setRadius = false;
	private float startX = 0, startY = 0;
	
	private Actions actions;

	public RemoteView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Get Screen Information
		Display display = ((Activity) this.getContext()).getWindowManager()
				.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;
		diffs = height > width ? height / 20 : width / 20;
		rotation = display.getRotation();
		textSize = ((rotation & 1) == 1) ? height / 7 : 52;
		textSize = (textSize < 52) ? 52 : textSize;
		textSize2 = textSize * 2;
		textSize3 = textSize * 3;
		textSize4 = textSize * 4;

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
		textPaint.setTextSize(textSize);
		textRightPaint = new Paint();
		textRightPaint.setColor(WHITE);
		textRightPaint.setAntiAlias(true);
		textRightPaint.setTextSize(textSize);
		textRightPaint.setTextAlign(Paint.Align.RIGHT);
		canvasPaint = new Paint(Paint.DITHER_FLAG);

		// Register to hear about media changes
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Check if battery should be updated
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

		if (currentAction > 0) {
			// Media Information
			canvas.drawText(title, 0, textSize, textPaint);
			canvas.drawText(artist, 0, textSize2, textPaint);
			canvas.drawText(album, 0, textSize3, textPaint);
		}

		// Update Action Information
		if (currentAction > 1)
			canvas.drawText(actions.getDirectionString(currentAction, getDirection()) + " " + actions.getAction(currentAction), 0,
					textSize4, textPaint);

		// Get Time and Battery
		Calendar c = Calendar.getInstance();
		canvas.drawText(
				(c.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "")
						+ c.get(Calendar.HOUR_OF_DAY) + ":"
						+ (c.get(Calendar.MINUTE) < 10 ? "0" : "")
						+ c.get(Calendar.MINUTE) + " (" + BatteryLevel + ")",
				width, height - textSize, textRightPaint);
	}

	public boolean onTouchEvent(MotionEvent event) {
		// How many fingers?
		int points = event.getPointerCount();

		// Did you remove your fingers?
		if (event.getAction() == MotionEvent.ACTION_UP) {
			actions.runAction(currentAction, getDirection());
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
	 * Get the direction (up or down).
	 * 
	 * @return The direction.
	 */
	private int getDirection() {
		float diff = startY - centerY;
		if (diff <= diffs && diff >= -diffs)
			return 1;

		if ((rotation & 1) == 0) {
			return startY > centerY ? 2 : 0;
		} else {
			return startY > centerY ? 0 : 2;
		}
	}

	/* ####################################################### */
	public void setBatteryValue(int BatteryLevelValue, boolean charging) {
		BatteryLevel = BatteryLevelValue + "%" + (charging ? "+" : "");
		if (BatteryLevelValue < 10) {
			this.textRightPaint.setColor(RED);
		} else if (BatteryLevelValue > 90 || charging) {
			this.textRightPaint.setColor(GREEN);
		} else {
			this.textRightPaint.setColor(WHITE);
		}
		invalidate();
	}

	public void setSongInformation(String title, String artist, String album) {
		this.title = title;
		this.artist = artist;
		this.album = album;
		invalidate();
	}
	
	public void setActions(Actions actions)
	{
		this.actions = actions;
	}
}
