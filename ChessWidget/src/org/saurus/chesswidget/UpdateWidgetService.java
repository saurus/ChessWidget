package org.saurus.chesswidget;

import java.util.ArrayList;
import java.util.Calendar;

import org.saurus.chess.pgn.Board;
import org.saurus.chesswidget.ChessData.PlayerData;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service {
	public class Sizes {
		public final int squareSizeInPx;
		public final int textSizeInSp;

		public Sizes(int squareSizeInPx, int textSizeInSp) {
			this.squareSizeInPx = squareSizeInPx;
			this.textSizeInSp = textSizeInSp;
		}

		@Override
		public String toString() {
			return "" + "sqPx=" + squareSizeInPx + ", txSp=" + textSizeInSp;
		}
	}

	public static final String MY_REQUIRE_REDRAW = "MY_REQUIRE_REDRAW";

	private static Calendar lastEvent;
	
	private static final String LOG = "chesswidget";
	private static float pixelsPerOneDp = 0.0f;
	private static float pixelsPerOneSp = 0.0f;
	private int count;
	private String lastUpdate = "(never)";
	private int pixelsPerSquare = 14;
	private ChessDataCache chessDataCache;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		boolean requireRedraw = false;

		Log.i(LOG, "onStartCommand(): " + count + ", intent is null: " + (intent == null ? "yes" : "no"));
		if (intent != null)
			requireRedraw = intent.getBooleanExtra(MY_REQUIRE_REDRAW, false);
		
		if (!requireRedraw) {
			// save current timestamp
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			lastEvent = calendar;
		}
		Context context = this.getApplicationContext();
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName thisWidget = new ComponentName(context, ChessWidget.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		if (stopIfNoWidgets(allWidgetIds))
			return super.onStartCommand(intent, flags, startId);

		boolean isWidgetInHome = false;
		boolean isWidgetInKeyguard = false;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			for (int widgetId : allWidgetIds) {
				if (checkIfLockScreen(appWidgetManager, widgetId))
					isWidgetInKeyguard = true;
				else
					isWidgetInHome = true;
			}
		} else
			isWidgetInHome = true;

		boolean widgetIsVisible = false;

		if (isWidgetInKeyguard) {
			KeyguardManager km = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);
			if (km.inKeyguardRestrictedInputMode()) {
				widgetIsVisible = true;
				Log.d(LOG, "onStartCommand(): widget in keyguard and keyguard active");
			}
		}

		if (widgetIsVisible == false && isWidgetInHome) {
			if (HomeIsShowing()) {
				widgetIsVisible = true;
				Log.d(LOG, "onStartCommand(): widget in home and home active");
			}
		}

		if (widgetIsVisible) {
			if (requireRedraw && chessDataCache != null && chessDataCache.getData() != null)
				drawBoard(chessDataCache.getData());
			else
				// get the board
				readBoardAsync();
		} else
			Log.d(LOG, "onStartCommand(): widget not visible, skipping...");

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Context context = this.getApplicationContext();
		Resources resources = context.getResources();
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();

		pixelsPerOneDp = displayMetrics.densityDpi / 160f;
		pixelsPerOneSp = displayMetrics.scaledDensity;
		pixelsPerSquare = resources.getInteger(R.integer.pixels_per_square);

		BoardFromNetAsync.enableHttpResponseCache(context);

		Log.v(LOG, "service onCreate(): " + count);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Read the board, using an AsyncTask, and caching the result
	 */
	private void readBoardAsync() {
		setStatus("reading...", false);

		if (chessDataCache == null)
			chessDataCache = ChessDataCache.fromUrlString(getString(R.string.pgn_url));

		new BoardFromNetAsync(this).execute(chessDataCache);
	}

	/**
	 * Called by the AsyncTask that reads the board, to set the new data
	 * 
	 * @param chessDataCache
	 *            board data and caching information
	 */

	public synchronized void setChessData(ChessDataCache chessDataCache) {
		this.chessDataCache = chessDataCache;
		if (chessDataCache == null) {
			Log.w(LOG, "setChessData(): skip update because got no data reader.");
			setStatus("[no cache]", false);
		} else if (chessDataCache.getData() == null) {
			Log.w(LOG, "setChessData(): skip update because got no data.");
			setStatus("[no data]", false);
		} else if (!chessDataCache.hasChanged()) {
			Log.i(LOG, "setChessData(): skip update because got cached data.");
			setStatus("(cached).", false);
		} else {
			// FIXME: check if we got a different board!
			count++;
			setStatus("(ok)", true);
			drawBoard(chessDataCache.getData());
		}
	}

	/**
	 * Draw the board
	 * 
	 * @param chessData
	 */
	private void drawBoard(ChessData chessData) {
		Context context = this.getApplicationContext();
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

		ComponentName thisWidget = new ComponentName(context, ChessWidget.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		if (stopIfNoWidgets(allWidgetIds))
			return;

		for (int widgetId : allWidgetIds) {
			Sizes sizes = new Sizes(pixelsPerSquare * 8, 10);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				sizes = calculateSizes(appWidgetManager, widgetId, sizes);

			boolean isLockScreen = false;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
				isLockScreen = checkIfLockScreen(appWidgetManager, widgetId);

			updateOneWidget(appWidgetManager, context, chessData, isLockScreen, widgetId, sizes);
		}

	}

	/**
	 * set the view of a specific widget.
	 * 
	 * @param appWidgetManager
	 * @param context
	 * @param chessData
	 * @param isLockScreen
	 * @param widgetId
	 * @param widthPx
	 * @param heightPx
	 */
	private void updateOneWidget(AppWidgetManager appWidgetManager, Context context, ChessData chessData, boolean isLockScreen, int widgetId, Sizes sizes) {
		String text;
		Board board = null;
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);

		try {
			text = getPlayerText(chessData.getWhite());
			remoteViews.setTextViewText(R.id.widget_textview_white, text);
			remoteViews.setFloat(R.id.widget_textview_white, "setTextSize", sizes.textSizeInSp);
			text = getPlayerText(chessData.getBlack());
			remoteViews.setTextViewText(R.id.widget_textview_black, text);
			remoteViews.setFloat(R.id.widget_textview_black, "setTextSize", sizes.textSizeInSp);

			board = chessData.getBoard();
		} catch (NullPointerException e) {
			text = "NullPointerException ";
			Log.e(LOG, e.toString());
		} catch (Exception e) {
			text = "Exception ";
			Log.e(LOG, e.toString());
		}

		BoardDrawer boardDrawer = new BoardDrawer(context, sizes.squareSizeInPx, sizes.squareSizeInPx);

		if (board != null) {
			boardDrawer.setBoard(board, chessData.getLastMove());

			Bitmap bmp = boardDrawer.draw();
			remoteViews.setImageViewBitmap(R.id.widget_imageview_chessboard, bmp);
		}
		appWidgetManager.updateAppWidget(widgetId, remoteViews);
	}

	/**
	 * Create a string suitable to show data for a player
	 * 
	 * @param player
	 * @return
	 */
	private String getPlayerText(PlayerData player) {
		if (player == null)
			return "???";
		String text = (player.isTurn() ? "* " : "") + player.getName() + ": " + player.getEvaluation();

		return text;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private boolean checkIfLockScreen(AppWidgetManager appWidgetManager, int widgetId) {
		boolean isLockScreen;
		{
			Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);

			int category = options.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);
			isLockScreen = category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;
		}
		return isLockScreen;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private Sizes calculateSizes(AppWidgetManager appWidgetManager, int widgetId, Sizes defaults) {
		Bundle appWidgetOptions = appWidgetManager.getAppWidgetOptions(widgetId);
		int portraitWidthDp = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, defaults.squareSizeInPx);
		int portraitHeightDp = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, defaults.squareSizeInPx);
		int landscapeWidthDp = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, defaults.squareSizeInPx);
		int landscapeHeightDp = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, defaults.squareSizeInPx);
		int widthDp = portraitWidthDp;
		int heightDp = portraitHeightDp;
		boolean isLandscape = false;

		DisplayMetrics displayMetrics = this.getApplicationContext().getResources().getDisplayMetrics();

		if (displayMetrics.widthPixels > displayMetrics.heightPixels)
			isLandscape = true;

		if (isLandscape) {
			widthDp = landscapeWidthDp;
			heightDp = landscapeHeightDp;
		}

		int sizeForFontSizing = Math.min(widthDp, heightDp);
		int extraSp = sizeForFontSizing / 70 - 2;

		int fontSp = defaults.textSizeInSp + extraSp;
		float fontDp = fontSp * pixelsPerOneSp / pixelsPerOneDp;
		int heightForText = (int) (2 * (fontDp + 2) + 16);

		// apply padding
		widthDp -= 4;
		heightDp -= 4;

		// leave some space for names
		heightDp = heightDp - heightForText;
		int sizeDp = Math.min(widthDp, heightDp);
		int sizePx = (int) (pixelsPerOneDp * sizeDp);

		sizePx = 8 * (sizePx / 8);

		Sizes sizes = new Sizes(sizePx, fontSp);
//		Log.v(LOG, "calculateSizes(): out: " //
//				+ "widthDp = " + widthDp + "\n" //
//				+ "defaults.squareSizeInPx = " + defaults.squareSizeInPx + "\n" //
//				+ "extraSp = " + extraSp + "\n" //
//				+ "fontSp = " + fontSp + "\n" //
//				+ "defaults.textSizeInSp = " //
//				+ defaults.textSizeInSp + "\n" //
//				+ "fontDp = " + fontDp + "\n" //
//				+ "heightForText = " + heightForText + "\n" //
//				+ "heightDp = " + heightDp + "\n" //
//				+ "sizeDp = " + sizeDp + "\n" //
//				+ "sizePx = " + sizePx + "\n");

		return sizes;
	}

	private void setStatus(String now, boolean doUpdateTimer) {
		Context context = this.getApplicationContext();
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
		ComponentName thisWidget = new ComponentName(getApplicationContext(), ChessWidget.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		if (stopIfNoWidgets(allWidgetIds))
			return;

		String prefix = "";
		if (doUpdateTimer) {
			Time today = new Time(Time.getCurrentTimezone());
			today.setToNow();
			lastUpdate = String.format("(%02d:%02d:%02d)", today.hour, today.minute, today.second);
			prefix = "U";
		}
		String text = "[" + count + "] " + prefix + lastUpdate + " " + now;
		remoteViews.setTextViewText(R.id.widget_textview_status, text);
		appWidgetManager.updateAppWidget(allWidgetIds, remoteViews);
	}

	// private boolean stopIfNoWidgets() {
	// Context context = this.getApplicationContext();
	// AppWidgetManager appWidgetManager =
	// AppWidgetManager.getInstance(context);
	// return stopIfNoWidgets(context, appWidgetManager);
	// }

	// private boolean stopIfNoWidgets(Context context, AppWidgetManager
	// appWidgetManager) {
	// ComponentName thisWidget = new ComponentName(context, ChessWidget.class);
	// int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
	// return stopIfNoWidgets(allWidgetIds);
	// }

	private boolean stopIfNoWidgets(int[] allWidgetIds) {
		if (allWidgetIds.length == 0) {
			Log.i(LOG, "stopIfNoWidgets(): no more Widgets found, stopping service.");
			stopSelf();
			return true;
		}

		return false;
	}

	public boolean HomeIsShowing() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		ArrayList<String> homeList = new ArrayList<String>();
		for (ResolveInfo info : this.getApplicationContext().getPackageManager().queryIntentActivities(intent, 0)) {
			homeList.add(info.activityInfo.packageName);
		}

		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningTaskInfo t : am.getRunningTasks(1)) {
			if (t != null && t.numRunning > 0) {
				ComponentName cn = t.baseActivity;
				if (cn == null)
					continue;
				else if (homeList.contains(cn.getPackageName()))
					return true;
			}
		}
		return false;
	}
	
	public static Calendar getLastEvent() {
		return lastEvent;
	}
}
