package com.example.cj.videoeditor.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtil {

    private static final String NAME = "batchvideo_sp";

    private static SharedPreferences getSp(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static void putString(Context context, String key, String value) {
        getSp(context).edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key, String def) {
        return getSp(context).getString(key, def);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        getSp(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean def) {
        return getSp(context).getBoolean(key, def);
    }

    public static void putInt(Context context, String key, int value) {
        getSp(context).edit().putInt(key, value).apply();
    }

    public static int getInt(Context context, String key, int def) {
        return getSp(context).getInt(key, def);
    }

    public static void clear(Context context) {
        getSp(context).edit().clear().apply();
    }
}
