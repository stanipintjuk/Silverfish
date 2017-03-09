package com.launcher.silverfish.shared;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.preference.PreferenceManager;

public class Settings {
    // Android doesn't seem to like static Context, so this
    // class cannot be a singleton with a .getInstance() method
    private final SharedPreferences mPrefs;

    private static final String KEY_WIDGET_VISIBLE = "widget_visible";
    private static final String KEY_WIDGET_PACKAGE_NAME = "widget_package_name";
    private static final String KEY_WIDGET_CLASS_NAME = "widget_class_name";
    private static final String KEY_PREVIOUSLY_STARTED = "pref_previously_started";
    private static final String KEY_LAST_OPEN_TAB = "pref_last_open_tab";
    private static final String KEY_DRAWER_BG_COLOR = "app_drawer_background_color";
    private static final String KEY_WIDGET_BG_COLOR = "widget_background_color";
    private static final String KEY_FONT_FG_COLOR = "font_foreground_color";

    private static final Boolean DEFAULT_WIDGET_VISIBLE = true;
    private static final Boolean DEFAULT_PREVIOUSLY_STARTED = false;
    private static final int DEFAULT_LAST_OPEN_TAB = 0;
    private static final int DEFAULT_DRAWER_BG_COLOR = 0x99000000; // "Dark tint"
    private static final int DEFAULT_WIDGET_BG_COLOR = 0x99000000; // "Dark tint"
    private static final int DEFAULT_FONT_FG_COLOR = 0xffffffff; // White

    // Tab selected border
    private static final int BUTTON_BORDER = 4;

    // Used to darken the tab buttons style
    private static final float ALPHA_DARKEN_PERCENTAGE = 0.15f;

    //region Constructor

    public Settings(final Context context) {
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

    public int getDrawerBgColor() {
        return mPrefs.getInt(KEY_DRAWER_BG_COLOR, DEFAULT_DRAWER_BG_COLOR);
    }

    public int getWidgetBgColor() {
        return mPrefs.getInt(KEY_WIDGET_BG_COLOR, DEFAULT_WIDGET_BG_COLOR);
    }

    public int getFontFgColor() {
        return mPrefs.getInt(KEY_FONT_FG_COLOR, DEFAULT_FONT_FG_COLOR);
    }

    public ComponentName getWidget() {
        String packageName = mPrefs.getString(KEY_WIDGET_PACKAGE_NAME, "");
        String className = mPrefs.getString(KEY_WIDGET_CLASS_NAME, "");

        return new ComponentName(packageName, className);
    }

    public Drawable getTabButtonStyle() {
        // stackoverflow.com/q/8308871
        // Darken the tint color twice so its different from the app drawer
        int tintColor = darkenAlpha(getDrawerBgColor(), ALPHA_DARKEN_PERCENTAGE);
        int darkTintColor = darkenAlpha(tintColor, ALPHA_DARKEN_PERCENTAGE);
        int solidColor = 0xFF000000 | tintColor; // OR the alpha bits to set them to 1

        // Store all the states here
        StateListDrawable stateListDrawable = new StateListDrawable();

        // All the shape drawables need a non-null shape
        final RectShape rectShape = new RectShape();

        // Pressed state (user holding their finger)
        ShapeDrawable pressedDrawable = new ShapeDrawable(rectShape);
        pressedDrawable.getPaint().setColor(solidColor);
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);

        // Selected state (category selected)
        // ShapeDrawable doesn't have Stroke, use GradientDrawable
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(darkTintColor);
        gd.setStroke(BUTTON_BORDER, getFontFgColor());

        // GradientDrawable doesn't have Inset, use LayerDrawable
        LayerDrawable ld = new LayerDrawable(new Drawable[] { gd });
        ld.setLayerInset(0, -BUTTON_BORDER, -BUTTON_BORDER, 0, -BUTTON_BORDER);
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, ld);

        // Normal state (tab not selected)
        ShapeDrawable normalDrawable = new ShapeDrawable(rectShape);
        normalDrawable.getPaint().setColor(tintColor);
        stateListDrawable.addState(new int[]{}, normalDrawable);

        return stateListDrawable;
    }

    //endregion

    //region Setters

    public void setWidgetVisible(final boolean visible) {
        mPrefs.edit().putBoolean(KEY_WIDGET_VISIBLE, visible).apply();
    }

    public void setPreviouslyStarted(final boolean firstTime) {
        mPrefs.edit().putBoolean(KEY_PREVIOUSLY_STARTED, firstTime).apply();
    }

    public void setLastOpenTab(final int tab) {
        mPrefs.edit().putInt(KEY_LAST_OPEN_TAB, tab).apply();
    }

    public void setDrawerBgColor(final int color) {
        mPrefs.edit().putInt(KEY_DRAWER_BG_COLOR, color).apply();
    }

    public void setWidgetBgColor(final int color) {
        mPrefs.edit().putInt(KEY_WIDGET_BG_COLOR, color).apply();
    }

    public void setFontFgColor(final int color) {
        mPrefs.edit().putInt(KEY_FONT_FG_COLOR, color).apply();
    }

    public void setWidget(String packageName, String className) {
        mPrefs.edit()
                .putString(KEY_WIDGET_PACKAGE_NAME, packageName)
                .putString(KEY_WIDGET_CLASS_NAME, className)
                .apply();
    }

    //endregion

    //region Private utilities

    private static int darkenAlpha(int color, float percentage) {
        int darken = Color.alpha(color) + (int)(percentage*255);
        if (darken > 255)
            darken = 255;

        return Color.argb(darken, Color.red(color), Color.green(color), Color.blue(color));
    }

    //endregion
}
