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

import android.app.AlertDialog;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;

import com.launcher.silverfish.dbmodel.TabTable;
import com.launcher.silverfish.sqlite.LauncherSQLiteHelper;

import java.util.ArrayList;
import java.util.LinkedList;

public class TabbedAppDrawerFragment extends Fragment {

    //region Fields

    TabHost tHost;

    private LauncherSQLiteHelper sqlHelper;
    private View rootView;
    private LinkedList<TabTable> arrTabs;
    private ArrayList<Button> arrButton;
    private android.support.v4.app.FragmentManager mFragmentManager;

    // Store the last open tab in RAM until onStop()
    // not to waste precious I/O every time a tab is changed.
    private int currentOpenTab = -1;

    //endregion

    //region Android lifecycle

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sqlHelper = new LauncherSQLiteHelper(getActivity().getBaseContext());

        rootView = inflater.inflate(R.layout.activity_app_drawer, container, false);
        mFragmentManager = getChildFragmentManager();

        tHost = (TabHost) rootView.findViewById(R.id.tabHost);
        tHost.setup();

        loadTabs();
        addOnClickListener();
        setOnDragListener();

        /** Defining Tab Change Listener event. This is invoked when tab is changed */
        TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                android.support.v4.app.FragmentTransaction ft = mFragmentManager.beginTransaction();

                // Detach all tab fragments from UI
                detachAllTabs(ft);

                // Then attach the relevant fragment.
                for (TabTable tab : arrTabs) {
                    int i = tab.id - 1;
                    if (tabId.equals(Integer.toString(i))) {
                        attachTabFragment(i, ft);
                        ft.commit();
                        return;
                    }
                }

                currentOpenTab = getLastTabId();
                attachTabFragment(currentOpenTab, ft);
                ft.commit();

            }
        };

        tHost.setOnTabChangedListener(tabChangeListener);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Open last opened tab
        currentOpenTab = getLastTabId();

        // If the tab is the same then onTabChanged won't be trigger,
        // so we have to add the fragment here
        if (currentOpenTab == tHost.getCurrentTab()) {
            android.support.v4.app.FragmentTransaction ft = mFragmentManager.beginTransaction();
            detachAllTabs(ft);

            attachTabFragment(currentOpenTab, ft);

            ft.commit();
        } else {
            setTab(currentOpenTab);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Save the last open tab
        if (currentOpenTab != -1)
            setLastTabId(currentOpenTab);
    }

    //endregion

    //region Tabs

    //region Attaching and detaching tabs

    private void attachTabFragment(int tabId, android.support.v4.app.FragmentTransaction ft) {
        // Every tab fragment should receive its ID as an argument
        Bundle args = new Bundle();
        args.putInt(Constants.TAB_ID, tabId);

        // Retrieve the fragment
        String fragment_tag = Integer.toString(tabId);
        AppDrawerTabFragment fragment = (AppDrawerTabFragment)mFragmentManager.findFragmentByTag(fragment_tag);

        // Attach it to the UI if an instance already exists, otherwise create a new instance and add it.
        if (fragment == null) {
            fragment = new AppDrawerTabFragment();
            fragment.setArguments(args);
            ft.add(R.id.realtabcontent, fragment, fragment_tag);
        } else {
            ft.attach(fragment);
        }
    }

    private void detachAllTabs(FragmentTransaction ft) {

        // Detach all tab fragments from UI
        for (TabTable tab : arrTabs) {
            int i = tab.id - 1;
            AppDrawerTabFragment fragment = (AppDrawerTabFragment) mFragmentManager.findFragmentByTag(Integer.toString(i));

            if (fragment != null)
                ft.detach(fragment);
        }
    }

    //endregion tabs

    //region Load tabs

    /**
     * Loads all tabs from the database.
     */
    private void loadTabs() {
        arrButton = new ArrayList<Button>();
        LinearLayout tabWidget = (LinearLayout)rootView.findViewById(R.id.custom_tabwidget);

        arrTabs = sqlHelper.getAllTabs();

        for (TabTable tab : arrTabs) {
            // Create a button for each tab
            Button btn = new Button(getActivity());
            btn.setText(tab.label);
            arrButton.add(btn);

            // Set the style of the button
            btn.setBackgroundResource(R.drawable.tab_style);
            btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            btn.setTextColor(Color.WHITE);

            // Add the button to the tab widget.
            tabWidget.addView(btn);

            // And create a new tab
            TabHost.TabSpec tSpecFragmentId = tHost.newTabSpec(Integer.toString(tab.id-1));
            tSpecFragmentId.setIndicator(tab.label);
            tSpecFragmentId.setContent(new DummyTabContent(getActivity().getBaseContext()));
            tHost.addTab(tSpecFragmentId);
        }
    }

    //endregion

    //region Set active tab

    private void setTab(int index) {
        currentOpenTab = index;
        tHost.setCurrentTab(index);

        // Toggle all the tab buttons to false
        for (Button button : arrButton) {
            button.setSelected(false);
        }

        // And then select the only relevant one
        arrButton.get(index).setSelected(true);
    }

    //endregion

    //region Get and set last tab id

    public int getLastTabId() {

        // If currentOpenTab is already loaded, do not try to load it from preferences again.
        if (currentOpenTab != -1) {
            return currentOpenTab;
        }

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity().getBaseContext());

        return prefs.getInt(getString(R.string.pref_last_open_tab), 0);
    }

    /**
     * Saves the last opened tab's id in the apps preferences
     * @param tabId
     */
    public void setLastTabId(int tabId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        SharedPreferences.Editor edit = prefs.edit();

        edit.putInt(getString(R.string.pref_last_open_tab), tabId);
        edit.apply();
    }

    //endregion

    //region Rename tab

    private void promptRenameTab(final int tabno) {

        // Find which tab we're renaming
        String tabName = arrButton.get(tabno).getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(String.format(getString(R.string.text_renaming_tab), tabName));

        // Set up the input
        final EditText input = new EditText(getContext());
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.text_rename), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                renameTab(tabno, input.getText().toString());
            }
        });
        builder.setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void renameTab(int tabno, String newName) {
        if (newName != null && !newName.isEmpty()) {
            // Update the name in the button for instant changes
            arrButton.get(tabno).setText(newName);

            // Update the name in the SQL database for the change to persist
            LauncherSQLiteHelper sql = new LauncherSQLiteHelper(getContext());
            sql.renameTab(tabno + 1, newName); // + 1 since it's not index-0 based
        }
    }

    //endregion

    //endregion

    //region Listeners

    private void addOnClickListener() {

        //make the tab buttons switch tab.
        for (TabTable tab : arrTabs) {

            Button btn = arrButton.get(tab.id-1);

            final int tabno = tab.id-1;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setTab(tabno);
                }
            });

            // Long click on the tab should prompt a box to rename it
            btn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    promptRenameTab(tabno);
                    return true;
                }
            });
        }
    }

    private void setOnDragListener() {

        rootView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {

                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED: {

                        // Care only about DRAG_APP_MOVE drags.
                        ClipDescription cd = dragEvent.getClipDescription();
                        if (!cd.getLabel().toString().equals(Constants.DRAG_APP_MOVE))
                            return false;

                        // Show the uninstall indicator
                        showUninstallIndicator();
                        break;
                    }

                    case DragEvent.ACTION_DRAG_ENTERED: {
                        // Don't do anything
                        break;
                    }

                    case DragEvent.ACTION_DRAG_LOCATION: {
                        // If drag is on the way out of this page then stop receiving drag events
                        int threshold = Constants.SCREEN_CORNER_THRESHOLD;
                        // Get display size
                        int screen_width = Utils.getScreenDimensions(getActivity()).x;
                        if (dragEvent.getX() > screen_width - threshold) {
                            return false;

                        } else {

                            // Check if the drag is hovering over a tab button
                            int i = getHoveringButton(dragEvent.getX(), dragEvent.getY());

                            // If so, change to that tab
                            if (i > -1)
                                setTab(i);
                        }
                        break;
                    }

                    case DragEvent.ACTION_DROP: {

                        // If app is dropped on the uninstall indicator uninstall the app
                        if (Utils.onBottomScreenEdge(getActivity(), dragEvent.getY())) {
                            String app_name = dragEvent.getClipData().getItemAt(0).getText().toString();
                            launchUninstallIntent(app_name);

                        } else {
                            // Retrieve the app name and place it in the tab.
                            String app_name = dragEvent.getClipData().getItemAt(0).getText().toString();
                            dropAppInTab(app_name);

                            int app_index = Integer.parseInt(
                                    dragEvent.getClipData().getItemAt(1).
                                            getText().toString());

                            int tab_id = Integer.parseInt(
                                    dragEvent.getClipData().getItemAt(2)
                                            .getText().toString());

                            // And remove it from the tab it came from
                            removeAppFromTab(app_index, tab_id);
                        }
                        break;
                    }

                    case DragEvent.ACTION_DRAG_ENDED: {
                        // Just hide the uninstall indicator
                        hideUninstallIndicator();
                        break;
                    }

                }
                return true;
            }

        });
    }

    //endregion

    //region Uninstall indicator

    private void showUninstallIndicator() {
        // Get the layout
        FrameLayout uninstall_indicator;
        uninstall_indicator = (FrameLayout) rootView.findViewById(R.id.uninstall_indicator);

        // Make it visible
        uninstall_indicator.setVisibility(View.VISIBLE);

        // And start the animation
        AlphaAnimation animation =  new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(500);
        uninstall_indicator.startAnimation(animation);
    }

    private void hideUninstallIndicator() {
        FrameLayout uninstall_indicator;
        uninstall_indicator = (FrameLayout)rootView.findViewById(R.id.uninstall_indicator);
        uninstall_indicator.setVisibility(View.INVISIBLE);
    }

    private void launchUninstallIntent(String package_name) {
        Uri packageUri = Uri.parse("package:"+package_name);
        Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        startActivity(uninstallIntent);
    }

    //endregion

    //region Apps moving

    private void removeAppFromTab(int appIndex, int tabId) {
        // Retrieve tab fragment
        android.support.v4.app.FragmentManager fm = getChildFragmentManager();
        AppDrawerTabFragment fragment = (AppDrawerTabFragment)
                fm.findFragmentByTag(Integer.toString(tabId));

        // Remove app and refresh the tab's layout
        fragment.removeApp(appIndex);
        //fragment.loadGridView();
    }

    private void dropAppInTab(String app_name) {
        // Retrieve tab fragment
        android.support.v4.app.FragmentManager fm = getChildFragmentManager();
        int tab_index = tHost.getCurrentTab();
        AppDrawerTabFragment fragment = (AppDrawerTabFragment)fm.findFragmentByTag(Integer.toString(tab_index));

        // Add app and refresh the tab's layout
        fragment.addApp(app_name);
        //fragment.loadGridView();
    }

    //endregion

    //region Utils

    /**
     * Returns the button which the (x, y) coordinates are inside of.
     * Otherwise returns -1.
     */
    private int getHoveringButton(float x, float y) {

        // Loop through all buttons and check if (x, y) is inside one of them
        for (int i = 0; i < arrButton.size(); i++) {

            Button btn = arrButton.get(i);

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

    //endregion
}
