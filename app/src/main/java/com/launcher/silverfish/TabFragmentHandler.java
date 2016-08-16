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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TabHost;

import com.launcher.silverfish.dbmodel.TabTable;
import com.launcher.silverfish.sqlite.LauncherSQLiteHelper;

import java.util.ArrayList;
import java.util.List;
import java.lang.IllegalAccessException;

/**
 * Created by Stanislav Pintjuk on 8/12/16.
 * E-mail: stanislav.pintjuk@gmail.com
 */
public class TabFragmentHandler {

    //region Fields

    private TabHost tHost;
    private FragmentManager mFragmentManager;
    private View rootView;
    private Activity mActivity;

    private List<TabInfo> arrTabs;
    private List<Button> arrButton;

    private TabButtonClickListener tabButtonClickListener;

    // Store the last open tab in RAM until end of lifecycle
    // to not waste precious I/O every time a tab is changed.
    private int currentOpenTab = -1;

    //endregion

    public TabFragmentHandler(FragmentManager fm, View view, Activity activity){

        mFragmentManager = fm;
        rootView = view;
        mActivity = activity;

        tHost = (TabHost) rootView.findViewById(R.id.tabHost);
        tHost.setup();

        loadTabs();
        setClickListener();

        /** Defining Tab Change Listener event. This is invoked when tab is changed */
        TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                android.support.v4.app.FragmentTransaction ft = mFragmentManager.beginTransaction();

                // Detach all tab fragments from UI
                detachAllTabs(ft);

                // Then attach the relevant fragment.
                for (TabInfo tab : arrTabs) {

                    if (tabId.equals(tab.getTag())) {
                        attachTabFragment(tab, ft);
                        ft.commit();
                        return;
                    }
                }

                currentOpenTab = getLastTabId();
                TabInfo current_tab = arrTabs.get(currentOpenTab);
                attachTabFragment(current_tab, ft);
                ft.commit();

            }
        };

        tHost.setOnTabChangedListener(tabChangeListener);

    }

    // region Android lifecycle


    // endregion

    // region Attaching and detaching tabs

    private void attachTabFragment(TabInfo tab, FragmentTransaction ft){
        // Retrieve the fragment
        String fragment_tag = tab.getTag();
        AppDrawerTabFragment fragment = (AppDrawerTabFragment)mFragmentManager
                                                              .findFragmentByTag(fragment_tag);

        // Attach it to the UI if an instance already exists, otherwise create a new instance and add it.
        if (fragment == null) {

            // send the tab id to each tab
            Bundle args = new Bundle();
            args.putInt(Constants.TAB_ID, tab.getId());

            fragment = new AppDrawerTabFragment();
            fragment.setArguments(args);

            ft.add(R.id.realtabcontent, fragment, fragment_tag);
        } else {
            ft.attach(fragment);
        }
    }

    private void detachAllTabs(FragmentTransaction ft) {

        // Detach all tab fragments from UI
        for (TabInfo tab : arrTabs) {
            AppDrawerTabFragment fragment = (AppDrawerTabFragment)
                                            mFragmentManager.findFragmentByTag(tab.getTag());

            if (fragment != null)
                ft.detach(fragment);
        }
    }

    // end region

    // region Load tabs

    /**
     * Loads all tabs from the database.
     */
    public void loadTabs(){
        arrButton = new ArrayList<Button>();
        arrTabs = new ArrayList<TabInfo>();

        LinearLayout tabWidget = (LinearLayout)rootView.findViewById(R.id.custom_tabwidget);

        LauncherSQLiteHelper sql = new LauncherSQLiteHelper(mActivity.getApplicationContext());
        List<TabTable> tabTables = sql.getAllTabs();

        for (TabTable tabEntry : tabTables) {
            TabInfo tab = new TabInfo(tabEntry);
            arrTabs.add(tab);

            // Create a button for each tab
            Button btn = new Button(mActivity.getApplicationContext());
            btn.setText(tab.getLabel());
            arrButton.add(btn);

            // Set the style of the button
            btn.setBackgroundResource(R.drawable.tab_style);
            btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                              ViewGroup.LayoutParams.MATCH_PARENT,
                                                              1));
            btn.setTextColor(Color.WHITE);

            // Add the button to the tab widget.
            tabWidget.addView(btn);

            // And create a new tab
            TabHost.TabSpec tSpecFragmentId = tHost.newTabSpec(tab.getTag());
            tSpecFragmentId.setIndicator(tab.getLabel());
            tSpecFragmentId.setContent(new DummyTabContent(mActivity.getBaseContext()));
            tHost.addTab(tSpecFragmentId);
        }

    }

    //endregion

    //region Set active tab

    public void setTab(int index) {
        currentOpenTab = index;
        tHost.setCurrentTab(index);

        selectTab(index);
    }

    private void selectTab(int index){
        // Toggle all the tab buttons to false
        for (Button button : arrButton) {
            button.setSelected(false);
        }

        // And then select the only relevant one
        arrButton.get(index).setSelected(true);
    }

    //endregion

    //region Get and set last tab id

    public void saveLastOpenTab(){
        // save the last tab
        if (currentOpenTab != -1){
            setLastTabId(currentOpenTab);
        }
    }

    public void loadLastOpenTab(){
        // Open last opened tab
        currentOpenTab = getLastTabId();

        // If this tab doesn't exist then simply go to the first tab.
        if (currentOpenTab >= arrTabs.size() || currentOpenTab < 0) {
            currentOpenTab = 0;
        }

        // If the tab is the same then onTabChanged won't be trigger,
        // so we have to add the fragment here
        if (currentOpenTab == tHost.getCurrentTab()) {
            android.support.v4.app.FragmentTransaction ft = mFragmentManager.beginTransaction();
            detachAllTabs(ft);

            TabInfo current_tab = arrTabs.get(currentOpenTab);
            attachTabFragment(current_tab, ft);

            ft.commit();

            // finally select the tab
            selectTab(currentOpenTab);

        } else {
            // let the TabHost handle attaching and detaching.
            setTab(currentOpenTab);
        }
    }

    private int getLastTabId() {

        // If currentOpenTab is already loaded, do not try to load it from preferences again.
        if (currentOpenTab != -1) {
            return currentOpenTab;
        }

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mActivity.getBaseContext());

        return prefs.getInt(mActivity.getString(R.string.pref_last_open_tab), 0);
    }

    /**
     * Saves the last opened tab's id in the apps preferences
     * @param tabId
     */
    private void setLastTabId(int tabId) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mActivity.getBaseContext());

        SharedPreferences.Editor edit = prefs.edit();

        edit.putInt(mActivity.getString(R.string.pref_last_open_tab), tabId);
        edit.apply();
    }

    //endregion

    //region click listener

    public void setOnTabButtonClickListener(TabButtonClickListener clickListener){
        tabButtonClickListener = clickListener;
    }

    private void setClickListener(){

        for (int i = 0; i < arrButton.size(); i++){
            Button btn = arrButton.get(i);
            final TabInfo tab = arrTabs.get(i);
            final int position = i;

            btn.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    if (tabButtonClickListener != null) {
                        tabButtonClickListener.onClick(tab, position);
                    }
                }
            });

            btn.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View view) {
                    if (tabButtonClickListener != null) {
                        return tabButtonClickListener.onLongClick(tab, position);
                    } else {
                        return false;
                    }
                }
            });
        }
    }
    //endregion

    //region Rename, remove, add tab

    public void addTab(String tab_name){
        if (tab_name == null || tab_name.isEmpty()) {
            throw new IllegalArgumentException("Tab name cannot be empty");
        } else {
            // add the tab to database
            LauncherSQLiteHelper sql = new LauncherSQLiteHelper(mActivity.getApplicationContext());
            TabTable tab_entry = sql.addTab(tab_name);

            final TabInfo tab = new TabInfo(tab_entry);
            arrTabs.add(tab);

            // create a button for the tab
            LinearLayout tabWidget = (LinearLayout)rootView.findViewById(R.id.custom_tabwidget);

            Button btn = new Button(mActivity.getApplicationContext());
            btn.setText(tab.getLabel());
            arrButton.add(btn);

            // Set the style of the button
            btn.setBackgroundResource(R.drawable.tab_style);
            btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1));
            btn.setTextColor(Color.WHITE);

            // Add the button to the tab widget.
            tabWidget.addView(btn);

            // And create a new tab
            TabHost.TabSpec tSpecFragmentId = tHost.newTabSpec(tab.getTag());
            tSpecFragmentId.setIndicator(tab.getLabel());
            tSpecFragmentId.setContent(new DummyTabContent(mActivity.getBaseContext()));
            tHost.addTab(tSpecFragmentId);

            final int tab_id = arrTabs.size() - 1;
            // add click listener to the button
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tabButtonClickListener.onClick(tab, tab_id);
                }
            });
            btn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return tabButtonClickListener.onLongClick(tab, tab_id);
                }
            });
        }

    }

    public void renameTab(TabInfo tab, int tab_index, String new_name) {
        if (new_name == null || new_name.isEmpty()){
            throw new IllegalArgumentException("Tab name cannot be empty");
        } else {
            // update name in database
            LauncherSQLiteHelper sql = new LauncherSQLiteHelper(mActivity.getApplicationContext());
            sql.renameTab(tab.getId(), new_name);

            // rename the button
            arrButton.get(tab_index).setText(new_name);

        }
    }

    public void removeTab(TabInfo tab, int tab_index){
        // don't allow the first tab to be removed
        if (tab_index == 0){
            throw new IllegalArgumentException("First tab is not allowed to be removed.");
        } else {
            // remove the tab from the database
            LauncherSQLiteHelper sql = new LauncherSQLiteHelper(mActivity.getApplicationContext());
            sql.removeTab(tab.getId());

            // hide the tab button
            Button btn = arrButton.get(tab_index);
            btn.setVisibility(View.GONE);

            //remove the tab fragment
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.remove(mFragmentManager.findFragmentByTag(tab.getTag()));

            //got to first tab
            setTab(0);
        }

    }

    //endregion

    //region Utils

    /**
     * Returns the index of th button which the (x, y) coordinates are inside of.
     * Returns -1 if there is none.
     */
    public int getHoveringTab(float x, float y){

        // Loop through all buttons and check if (x, y) is inside one of them
        for (int i = 0; i < arrButton.size(); i++) {

            // ignore all tab buttons that are removed
            Button btn = arrButton.get(i);
            if (btn.getVisibility() == View.GONE){
                continue;
            }

            // Get the geometry
            float high_x = btn.getX();
            float high_y = btn.getY();
            float low_x = btn.getX()+btn.getWidth();
            float low_y = btn.getY()+btn.getHeight();

            // Check if (x, y) is inside
            if (x > high_x && x < low_x && y > high_y && y < low_y) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the current open tab
     * @return
     */
    public TabInfo getCurrentTab(){
        return arrTabs.get(currentOpenTab);
    }

    //endregion
}
