package org.easyaccess;

public class Log {

	public static int DEBUG = 1;
	public static int PRODUCTION = 2;

	private static int logLevel = PRODUCTION;

	private static String tag = "idealepub";

	public static void setLogLevel(int level) {
		logLevel = level;
	}

	public static void setLogTag(String t) {
		tag = t;
	}

	public static void w(Exception e) {
		if (logLevel == DEBUG) {
			android.util.Log.w(tag, e);
		}
	}

	public static void w(String tag, Exception e) {
		if (logLevel == DEBUG) {
			android.util.Log.w(tag, e);
		}
	}

	public static void d(String msg) {
		if (logLevel == DEBUG) {
			android.util.Log.d(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (logLevel == DEBUG) {
			android.util.Log.d(tag, msg);
		}
	}

}
