package org.saurus.chesswidget;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import org.saurus.net.get.HttpGetCached;

import android.util.Log;

public class ChessDataCache extends HttpGetCached<ChessData> {

	private ChessDataCache(String urlString) throws MalformedURLException {
		super(urlString);
	}

	public ChessDataCache(URL url, String lastModified, ChessData data) {
		super(url, lastModified, data);
	}

	private static final String LOG = "chesswidget";

	public static ChessDataCache fromUrlString(String urlString) {
		try {
			return new ChessDataCache(urlString);
		} catch (MalformedURLException e) {
			Log.w(LOG, "MalformedURLException!!");
			return null;
		}
	}
	
	@Override
	protected ChessDataCache convert(Reader reader, String lastModified) {
		ChessData chessData = null;

		try {
			if (reader != null) {
				chessData = new ChessData();

				if (!chessData.Read(reader))
					chessData = null;

				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		} catch (Exception e) {
			Log.w("chesswidget", "exception in ChessDataReader.convert(): " + e);
			chessData = null;
		}

		if (chessData == null)
			return null;

		return new ChessDataCache(this.getUrl(), lastModified, chessData);
	}

	@Override
	protected void log(String msg) {
		Log.i(LOG, msg);
	}
}
