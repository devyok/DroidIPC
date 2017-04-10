package com.devy.droidipc;

import android.util.Log;

public final class LogControler {

	private static boolean DEBUG = false;
	
	public static final String TAG = "ipctrace";
	
	public enum Level {
		INFO,WARNING,ERROR
	}
	
	public static void enableDebug(){
		DEBUG = true;
	}
	
	public static void disableDebug(){
		DEBUG = false;
	}
	
	public static void print(final Level level,final String log) {
		if(DEBUG) return ;
		switch (level) {
		case INFO:
			Log.i(TAG, log);
			break;
		case WARNING:
			Log.w(TAG, log);
			break;
		case ERROR:
			Log.e(TAG, log);
			break;
		default:
			break;
		}
	}
	
}
