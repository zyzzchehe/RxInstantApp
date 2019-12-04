package com.rocktech.sharebookcase.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SpHelper {

	static SharedPreferences sp;

	public static void init(Context context) {
		sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
	}

	public static String getStringValue(String key) {
		return sp.getString(key, null);
	}

	public static String getStringValue(String key, String defaultValue) {
		return sp.getString(key, defaultValue);
	}

	public static void setStringValue(String key, String value) {
		Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static boolean getBooleanValue(String key,boolean defaultvalue) {
		return sp.getBoolean(key, defaultvalue);
	}

	public static void setBooleanValue(String key, boolean value) {
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static int getIntValue(String key) {
		return sp.getInt(key, 0);
	}

	public static void setIntValue(String key, int value) {
		Editor editor = sp.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	public static float getFloatValue(String key) {
		return sp.getFloat(key, 0.0f);
	}
	
	public static void setFloatValue(String key, float value) {
		Editor editor = sp.edit();
		editor.putFloat(key, value);
		editor.commit();
	}
}
