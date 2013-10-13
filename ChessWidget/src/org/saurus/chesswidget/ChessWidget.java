package org.saurus.chesswidget;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class ChessWidget extends AppWidgetProvider {
	private static final String LOG = "chesswidget";
	private static String MY_WIDGET_UPDATE = "MY_OWN_WIDGET_UPDATE";
	public static String MY_WIDGET_CONFIGURED = "MY_OWN_WIDGET_CONFIGURED";
	private static String APPWIDGET_RESIZE = "com.sec.android.widgetapp.APPWIDGET_RESIZE";
	private static boolean isRegisteredFromCode = false;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.v(LOG, "onUpdate(): num. widgets: " + appWidgetIds.length);

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String updateFrequencyWifi = sharedPref.getString(ChessWidgetConfigure.UPDATE_FREQUENCY_WIFI, "");
		boolean isOk = sharedPref.getBoolean(ChessWidgetConfigure.CONFIGURATION_OK, false);

		if (!isOk) {
			Log.i(LOG, "onUpdate() called but configuration not finished.");
			return;
		}
		// updateAppWidget(context, appWidgetManager, appWidgetIds);

		if (!isRegisteredFromCode) {
			isRegisteredFromCode = true;

			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			context.getApplicationContext().registerReceiver(this, filter);
		}

		if (updateFrequencyWifi.equals(""))
			return;

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
		// on click on board, go to homepage
		Uri uri = Uri.parse(context.getString(R.string.site_url));
		Intent gotoHomeIntent = new Intent(Intent.ACTION_VIEW, uri);
		PendingIntent gotoHomePendingIntent = PendingIntent.getActivity(context, 0, gotoHomeIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.widget_imageview_chessboard, gotoHomePendingIntent);

		// on click on white name, do a refresh now
		Intent clickIntent = new Intent(context, ChessWidget.class);

		clickIntent.setAction(MY_WIDGET_UPDATE);// AppWidgetManager.ACTION_APPWIDGET_UPDATE
		// clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
		// allWidgetIds);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.widget_textview_white, pendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
		createAlarm(context);
	}

	public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, boolean forceRedraw) {
		Log.v(LOG, "updateAppWidget()");
		// Build the intent to call the service
		Intent intent = new Intent(context, UpdateWidgetService.class);// .getApplicationContext()
		if (forceRedraw)
			intent.putExtra(UpdateWidgetService.MY_REQUIRE_REDRAW, true);
		// Update the widgets via the service
		context.startService(intent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		String intentAction = intent.getAction();
		Log.v(LOG, "onReciever(): action = " + intentAction);

		if (MY_WIDGET_CONFIGURED.equals(intentAction)) {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ComponentName thisWidget = new ComponentName(context, ChessWidget.class);
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
			onUpdate(context, appWidgetManager, appWidgetIds);
		} else if (MY_WIDGET_UPDATE.equals(intentAction)) {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			updateAppWidget(context, appWidgetManager, false);
		} else if (APPWIDGET_RESIZE.equals(intentAction)) {
			// Handle TouchWiz
			handleTouchWiz(context, intent);

		} else if (Intent.ACTION_SCREEN_OFF.equals(intentAction)) {
			cancelAlarm(context);
			// the screen is going off, stop timer
		} else if (Intent.ACTION_SCREEN_ON.equals(intentAction)) {
			// the screen is turning on, (re-)start timer
			createAlarm(context);
		}
		super.onReceive(context, intent);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void handleTouchWiz(Context context, Intent intent) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

		int appWidgetId = intent.getIntExtra("widgetId", 0);
		int widgetSpanX = intent.getIntExtra("widgetspanx", 0);
		int widgetSpanY = intent.getIntExtra("widgetspany", 0);

		if (appWidgetId > 0 && widgetSpanX > 0 && widgetSpanY > 0) {
			Bundle newOptions = new Bundle();
			// We have to convert these numbers for future use
			newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, widgetSpanY * 74);
			newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widgetSpanX * 74);

			onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
		}
	}

	@Override
	public void onDisabled(Context context) {
		// Intent intent = new Intent(context.getApplicationContext(),
		// UpdateWidgetService.class);
		cancelAlarm(context);
		Log.v(LOG, "onDisabled()");
		super.onDisabled(context);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		Log.v(LOG, "onAppWidgetOptionsChanged()");
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

		updateAppWidget(context, appWidgetManager, true);
	}

	private void createAlarm(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String updateFrequencyWifiStr = sharedPref.getString(ChessWidgetConfigure.UPDATE_FREQUENCY_WIFI, "1");
		int updateFrequencyWifi = Integer.parseInt(updateFrequencyWifiStr);
		// prepare Alarm Service to trigger Widget
		Log.v(LOG, "createAlarm()");
		Intent intent = new Intent(MY_WIDGET_UPDATE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, 1);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), updateFrequencyWifi * 60 * 1000, pendingIntent);
	}

	private void cancelAlarm(Context context) {
		Log.v(LOG, "cancelAlarm()");
		Intent intent = new Intent(MY_WIDGET_UPDATE);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}
}
