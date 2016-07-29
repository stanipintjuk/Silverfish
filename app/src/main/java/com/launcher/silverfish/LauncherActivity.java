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

package com.launcher.silverfish;

import android.content.ClipDescription;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.View;

import com.launcher.silverfish.dbmodel.TabTable;
import com.launcher.silverfish.sqlite.LauncherSQLiteHelper;

/**
 * This is the main activity of the launcher
 */
public class LauncherActivity extends FragmentActivity {
    LauncherPagerAdapter mCollectionPagerAdapter;
    ViewPager mViewPager;

    // Used for telling home screen when a shortcut is added.
    private ShortcutAddListener shortcutAddListener;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        // Check if the app is started for the first time. If it is then we have to
        // populate the database with some default values.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false);
        if(!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
            edit.commit();
            createDefaultTabs();
        }

        // Create the pager
        mCollectionPagerAdapter =
                new LauncherPagerAdapter(
                        getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCollectionPagerAdapter);
        mViewPager.setCurrentItem(1);

        setDragListener();
    }

    private void createDefaultTabs() {
        LauncherSQLiteHelper sql = new LauncherSQLiteHelper(this.getBaseContext());
        TabTable tab0 = new TabTable();
        TabTable tab1 = new TabTable();
        TabTable tab2 = new TabTable();
        TabTable tab3 = new TabTable();
        TabTable tab4 = new TabTable();
        TabTable tab5 = new TabTable();
        TabTable tab6 = new TabTable();

        tab0.label=getString(R.string.tab_other);
        tab1.label=getString(R.string.tab_phone);
        tab2.label=getString(R.string.tab_games);
        tab3.label=getString(R.string.tab_internet);
        tab4.label=getString(R.string.tab_media);
        tab5.label=getString(R.string.tab_accessories);
        tab6.label=getString(R.string.tab_settings);

        sql.addTab(tab0);
        sql.addTab(tab1);
        sql.addTab(tab2);
        sql.addTab(tab3);
        sql.addTab(tab4);
        sql.addTab(tab5);
        sql.addTab(tab6);
    }

    private void setDragListener(){
        mViewPager.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()){
                    case DragEvent.ACTION_DRAG_STARTED:
                        // Only care about the DRAG_APP_MOVE description.
                        ClipDescription cd = dragEvent.getClipDescription();
                        if (!cd.getLabel().toString().equals(Constants.DRAG_APP_MOVE))
                            return false;
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        //Dont do anything
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        changePage(dragEvent);
                        break;
                    case DragEvent.ACTION_DROP:
                        dropItem(dragEvent);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        //Dont do anything
                        break;

                }
                return true;
            }
        });

    }

    private void dropItem(DragEvent dragEvent){
        if (mViewPager.getCurrentItem() == 1){
            String app_name = dragEvent.getClipData().getItemAt(0).getText().toString();

            if(getFragmshortcutAddListenertRefreshListener() != null){
                getFragmshortcutAddListenertRefreshListener().OnShortcutAdd(app_name);
            }

        }
    }

    private void changePage(DragEvent dragEvent){
        // Change page mid drag if drag is within threshold
        int threshold = Constants.SCREEN_CORNER_THRESHOLD;

        // get display size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        // Change page
        if (mViewPager.getCurrentItem() == 0 && dragEvent.getX() >= width - threshold){
            mViewPager.setCurrentItem(1);
        }else if(mViewPager.getCurrentItem() == 1 && dragEvent.getX() <= threshold){
            mViewPager.setCurrentItem(0);
        }

    }

    public ShortcutAddListener getFragmshortcutAddListenertRefreshListener(){
        return shortcutAddListener;
    }

    public void setFragmshortcutAddListenertRefreshListener(ShortcutAddListener shortcutAddListener){
        this.shortcutAddListener= shortcutAddListener;

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Override the functionality of back and home key
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)
                && event.getRepeatCount() == 0) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Override the functionality of back and home key
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)&& event.isTracking()
                && !event.isCanceled()) {
            mViewPager.setCurrentItem(1);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}

