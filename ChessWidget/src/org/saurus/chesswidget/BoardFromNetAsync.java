package org.saurus.chesswidget;

import java.io.File;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class BoardFromNetAsync extends AsyncTask<ChessDataCache, Void, ChessDataCache> {
	private UpdateWidgetService service;

	public BoardFromNetAsync(UpdateWidgetService service) {
		super();
		this.service = service;
	}

	@Override
	protected ChessDataCache doInBackground(ChessDataCache... params) {
		ChessDataCache chessDataReader = params[0];
		return (ChessDataCache)chessDataReader.get();
	}

	@Override
	protected void onPostExecute(ChessDataCache chessDataReader) {
		super.onPostExecute(chessDataReader);

		this.service.setChessData(chessDataReader);
	}

	public static void enableHttpResponseCache(Context context) {
		try {
			Log.v("chesswidget", "enabling http cache");
			long httpCacheSize = 1 * 1024 * 1024; // 1 MiB
			File httpCacheDir = new File(context.getCacheDir(), "http");
			Class.forName("android.net.http.HttpResponseCache").getMethod("install", File.class, long.class).invoke(null, httpCacheDir, httpCacheSize);
		} catch (Exception httpResponseCacheNotAvailable) {
			Log.w("chesswidget", "enable http cache failed: " + httpResponseCacheNotAvailable);
		}
	}
}
