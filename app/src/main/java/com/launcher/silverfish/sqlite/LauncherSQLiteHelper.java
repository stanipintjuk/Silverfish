/*
 * Copyright 2016 Stanislav Pintjuk
 * E-mail: stanislav.pintjuk@gmail.com
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.launcher.silverfish.sqlite;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.launcher.silverfish.ShortcutDetail;
import com.launcher.silverfish.dbmodel.TabTable;

import java.util.LinkedList;

public class LauncherSQLiteHelper extends SQLiteOpenHelper {

    // Database information
    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="LauncherDB";

    //tables
    private static final String TABLE_TABS = "tabs";
    private static final String TABLE_APPS = "apps";
    private static final String TABLE_SHORTCUTS = "shortcuts";
    private static final String TABLE_WIDGET = "widget";

    // keys
    private static final String KEY_ID = "id";
    private static final String KEY_TAB_ID = "tab_id";
    private static final String KEY_LABEL = "label";
    private static final String KEY_PACKAGE_NAME = "package_name";
    private static final String KEY_CLASS_NAME = "class_name";


    public LauncherSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create the tab table
        String CREATE_TAB_TABLE = "CREATE TABLE "+ TABLE_TABS +" ( "+
                KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_LABEL+" TEXT )";

        // Create the apps table
        String CREATE_APPS_TABLE = "CREATE TABLE "+ TABLE_APPS +" (" +
                KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_PACKAGE_NAME+" TEXT, " +
                KEY_TAB_ID+" INTEGER )";

        String CREATE_SHORTCUTS_TABLE = "CREATE TABLE "+TABLE_SHORTCUTS+" (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_PACKAGE_NAME + " TEXT )";

        String CREATE_WIDGET_TABLE = "CREATE TABLE "+TABLE_WIDGET+" (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_PACKAGE_NAME + " TEXT," +
                KEY_CLASS_NAME + " TEXT )";

        sqLiteDatabase.execSQL(CREATE_TAB_TABLE);
        sqLiteDatabase.execSQL(CREATE_APPS_TABLE);
        sqLiteDatabase.execSQL(CREATE_SHORTCUTS_TABLE);
        sqLiteDatabase.execSQL(CREATE_WIDGET_TABLE);

        ContentValues values = new ContentValues();
        values.put(KEY_PACKAGE_NAME, "");
        values.put(KEY_CLASS_NAME, "");

        sqLiteDatabase.insert(TABLE_WIDGET, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop all tables when update - for now...
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_TABS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_APPS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_SHORTCUTS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_WIDGET);

        // create fresh tables
        this.onCreate(sqLiteDatabase);
    }

    public TabTable addTab(String tab_name) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LABEL, tab_name);

        long tab_id = db.insert(TABLE_TABS,
                        null,
                        values);

        db.close();

        // construct the tab to return
        TabTable tab = new TabTable();
        tab.id = (int)tab_id;
        tab.label = tab_name;

        return tab;
    }

    public void removeTab(int tab_id) {
        SQLiteDatabase db = getWritableDatabase();

        // remove tab from database
        db.delete(TABLE_TABS,
                KEY_ID + " = " + Integer.toString(tab_id),
                null);

        // remove all apps from the tab
        db.delete(TABLE_APPS,
                KEY_TAB_ID + " = " + Integer.toString(tab_id),
                null);

        db.close();
    }

    public int renameTab(int tab_id, String new_name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LABEL, new_name);

        int i = db.update(TABLE_TABS,
                values,
                KEY_ID+" = "+Integer.toString(tab_id),
                null);
        db.close();
        return i;
    }

    public LinkedList<TabTable> getAllTabs() {
        LinkedList<TabTable> tabs = new LinkedList<TabTable>();

        // Select all tabs from database
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM "+ TABLE_TABS;
        Cursor cursor = db.rawQuery(query, null);

        TabTable tab = null;
        if (cursor.moveToFirst()) {
           do {
               tab = new TabTable();
               tab.id = Integer.parseInt(cursor.getString(0));
               tab.label = cursor.getString(1);

               tabs.add(tab);
           } while (cursor.moveToNext());
        }
        return tabs;
    }

    public LinkedList<String> getAppsForTab(int tab_id) {
        LinkedList<String> app_names = new LinkedList<String>();

        SQLiteDatabase db = getReadableDatabase();

        String selection = KEY_TAB_ID+" = "+Integer.toString(tab_id);
        Cursor cursor =
                db.query(TABLE_APPS,
                        new String[]{KEY_PACKAGE_NAME},
                        selection,
                        null,
                        null,
                        null,
                        null);

        String app_name = null;
        if (cursor.moveToFirst()) {
            do {
                app_name = cursor.getString(0);
                app_names.add(app_name);
            }while(cursor.moveToNext());
        }
        return app_names;
    }

    public LinkedList<String> getAllApps() {
        LinkedList<String> app_names = new LinkedList<String>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_APPS,
                        new String[]{KEY_PACKAGE_NAME},
                        null,
                        null,
                        null,
                        null,
                        null);

        String app_name = null;
        if (cursor.moveToFirst()) {
            do {
                app_name = cursor.getString(0);
                app_names.add(app_name);
            }while(cursor.moveToNext());
        }
        return app_names;
    }

    public void addAppToTab(String app_name, int tab_id) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PACKAGE_NAME, app_name);
        values.put(KEY_TAB_ID, Integer.toString(tab_id));

        db.insert(TABLE_APPS, null, values);
        db.close();
    }

    public void removeAppFromTab(String app_name, int tab_id) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_APPS,
                KEY_PACKAGE_NAME + " = ? AND "+
                        KEY_TAB_ID+" = ?",
                new String[]{app_name, Integer.toString(tab_id)});
        db.close();
    }

    public long addShortcut(String app_name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PACKAGE_NAME, app_name);

        long id = db.insert(TABLE_SHORTCUTS, null, values);
        db.close();

        return id;
    }

    public void removeShorcut(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SHORTCUTS, KEY_ID + " = " + Long.toString(id),null);
        db.close();
    }

    public LinkedList<ShortcutDetail> getAllShortcuts() {
        LinkedList<ShortcutDetail> shortcuts = new LinkedList<ShortcutDetail>();

        String query = "SELECT * FROM "+TABLE_SHORTCUTS;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                ShortcutDetail shortcut = new ShortcutDetail();
                shortcut.name = cursor.getString(1);
                shortcut.id = cursor.getInt(0);
                shortcuts.add(shortcut);
            }while(cursor.moveToNext());
        }

        return shortcuts;
    }

    public ComponentName getWidgetContentName() {
        SQLiteDatabase db = getReadableDatabase();

        String selection = KEY_ID+" = 1";
        Cursor cursor = db.query(TABLE_WIDGET,
                new String[]{KEY_PACKAGE_NAME, KEY_CLASS_NAME},
                selection,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();
        ComponentName cn = new ComponentName(cursor.getString(0), cursor.getString(1));

        return cn;
    }

    public int updateWidget(String pkg, String cls) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PACKAGE_NAME, pkg);
        values.put(KEY_CLASS_NAME, cls);

        int i = db.update(TABLE_WIDGET,
                values,
                KEY_ID+ " = 1",
                null);
        db.close();
        return i;
    }

}

