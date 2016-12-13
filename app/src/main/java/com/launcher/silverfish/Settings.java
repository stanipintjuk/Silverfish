package com.launcher.silverfish;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
    // Android doesn't seem to like static Context, so this
    // class cannot be a singleton with a .getInstance() method
    private SharedPreferences mPrefs;

    private static final String KEY_WIDGET_VISIBLE = "widget_visible";
    private static final String KEY_PREVIOUSLY_STARTED = "pref_previously_started";
    private static final String KEY_LAST_OPEN_TAB = "pref_last_open_tab";

    private static final Boolean DEFAULT_WIDGET_VISIBLE = true;
    private static final Boolean DEFAULT_PREVIOUSLY_STARTED = false;
    private static final int DEFAULT_LAST_OPEN_TAB = 0;

    //region Constructor

    public Settings(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //endregion

    //region Getters

    public boolean isWidgetVisible() {
        return mPrefs.getBoolean(KEY_WIDGET_VISIBLE, DEFAULT_WIDGET_VISIBLE);
    }

    public boolean wasPreviouslyStarted() {
        return mPrefs.getBoolean(KEY_PREVIOUSLY_STARTED, DEFAULT_PREVIOUSLY_STARTED);
    }

    public int getLastOpenTab() {
        return mPrefs.getInt(KEY_LAST_OPEN_TAB, DEFAULT_LAST_OPEN_TAB);
    }

    //endregion

    //region Setters

    public void setWidgetVisible(boolean visible) {
        mPrefs.edit().putBoolean(KEY_WIDGET_VISIBLE, visible).apply();
    }

    public void setPreviouslyStarted(boolean firstTime) {
        mPrefs.edit().putBoolean(KEY_PREVIOUSLY_STARTED, firstTime).apply();
    }

    public void setLastOpenTab(int tab) {
        mPrefs.edit().putInt(KEY_LAST_OPEN_TAB, tab).apply();
    }

    //endregion
}
