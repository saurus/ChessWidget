package org.saurus.chesswidget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class ChessWidgetConfigure extends PreferenceActivity {
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	
	public static final String UPDATE_FREQUENCY_WIFI = "pref_updateFrequencyWifi";
	public static final String UPDATE_FREQUENCY_MOBILE = "pref_updateFrequencyMobile";

	public static final String CONFIGURATION_OK = "pref_isOk";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		// PreferenceManager localPrefs = getPreferenceManager();
		// localPrefs.setSharedPreferencesName("GITC_Prefs");
		addPreferencesFromResource(R.xml.preferences);

		// Find the widget id from the intent.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}
		setOk(false);
	}
	
    @Override
    public void onBackPressed() {
		setOk(true);
        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
        
        //Intent i = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, ChessWidget.class);
        //Intent i = new Intent(ChessWidget.MY_WIDGET_CONFIGURED);
        Intent i = new Intent(ChessWidget.MY_WIDGET_CONFIGURED, null, this, ChessWidget.class);
        sendBroadcast(i);
    }

	private void setOk(boolean value) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor= sharedPref.edit();
		editor.putBoolean(CONFIGURATION_OK, value);
		editor.commit();
	}
}
