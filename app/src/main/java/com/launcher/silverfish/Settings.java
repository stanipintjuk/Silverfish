package com.launcher.silverfish;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
    // Android doesn't seem to like static Context, so this
    // class cannot be a singleton with a .getInstance() method
    private Context mContext;
    private SharedPreferences mPrefs;

    interface Event {
        void onPropertyChanged(String propertyKey, Object value);
    }

    public static final String KEY_WIDGET_VISIBLE = "widget_visible";

    private static final Boolean DEFAULT_WIDGET_VISIBLE = true;

    public Settings(Context context) {
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isWidgetVisible() {
        return mPrefs.getBoolean(KEY_WIDGET_VISIBLE, DEFAULT_WIDGET_VISIBLE);
    }

    public void setWidgetVisible(boolean visible) {
        mPrefs.edit().putBoolean(KEY_WIDGET_VISIBLE, visible).apply();
    }
}
