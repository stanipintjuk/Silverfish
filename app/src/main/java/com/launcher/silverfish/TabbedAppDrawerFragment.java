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

public class TabbedAppDrawerFragment extends Fragment{

    private LauncherSQLiteHelper sqlhelper;
    TabHost tHost;
    private View rootView;
    private LinkedList<TabTable> arrTabs;
    private ArrayList<Button> arrButton;

    // store the last open tab in RAM until onStop()
    // to not waste precious I/O every time a tab is changed.
    private int current_open_tab = -1;

    private android.support.v4.app.FragmentManager mFragmentManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        sqlhelper = new LauncherSQLiteHelper(getActivity().getBaseContext());

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

                // detach all tab fragments from UI
                detachAllTabs(ft);

                // then attach the relevant fragment.
                for (TabTable tab : arrTabs) {
                    int i = tab.id - 1;
                    if (tabId.equals(Integer.toString(i))) {
                        attachTabFragment(i, ft);
                        ft.commit();
                        return;
                    }
                }

                current_open_tab = getLastTabId();
                attachTabFragment(current_open_tab, ft);
                ft.commit();

            }
        };

        tHost.setOnTabChangedListener(tabChangeListener);

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();

        // Open last opened tab
        current_open_tab = getLastTabId();

        // if the tab is the same then onTabChanged won't be trigger,
        // so we have to add the fragment here
        if (current_open_tab == tHost.getCurrentTab()){
            android.support.v4.app.FragmentTransaction ft = mFragmentManager.beginTransaction();
            detachAllTabs(ft);

            attachTabFragment(current_open_tab, ft);

            ft.commit();
        }else{
            setTab(current_open_tab);
        }
    }

    @Override
    public void onStop(){
        super.onStop();

        // save the last open tab
        if (current_open_tab != -1)
            setLastTabId(current_open_tab);

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

    private void attachTabFragment(int tab_id, android.support.v4.app.FragmentTransaction ft){
        // Every tab fragment should receive its ID as an argument
        Bundle args = new Bundle();
        args.putInt(Constants.TAB_ID, tab_id);

        // retrieve the fragment
        String fragment_tag = Integer.toString(tab_id);
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

    private void setOnDragListener(){

        rootView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {


                switch (dragEvent.getAction()){
                    case DragEvent.ACTION_DRAG_STARTED: {

                        // Care only about DRAG_APP_MOVE drags.
                        ClipDescription cd = dragEvent.getClipDescription();
                        if (!cd.getLabel().toString().equals(Constants.DRAG_APP_MOVE))
                            return false;

                        // show the uninstall indicator
                        showUninstallIndicator();
                        break;
                    }

                    case DragEvent.ACTION_DRAG_ENTERED: {
                        //Don't do anything
                        break;
                    }

                    case DragEvent.ACTION_DRAG_LOCATION: {
                        // If drag is on the way out of this page then stop receiving drag events
                        int threshold = Constants.SCREEN_CORNER_THRESHOLD;
                        // get display size
                        int screen_width = Utils.getScreenDimensions(getActivity()).x;
                        if (dragEvent.getX() > screen_width - threshold) {
                            return false;

                        } else {

                            // check if the drag is hovering over a tab button
                            int i = getHoveringButton(dragEvent.getX(), dragEvent.getY());

                            // if so - change to that tab
                            if (i > -1)
                                setTab(i);
                        }
                        break;
                    }

                    case DragEvent.ACTION_DROP: {

                        // if app is dropped on the uninstall indicator uninstall the app
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

                            // and remove it from the tab it came from
                            removeAppFromTab(app_index, tab_id);
                        }
                        break;
                    }

                    case DragEvent.ACTION_DRAG_ENDED: {
                        // just hide the uninstall indactor
                        hideUninstallIndicator();
                        break;
                    }

                }
                return true;
            }

        });
    }

    private void launchUninstallIntent(String package_name){
        Uri packageUri = Uri.parse("package:"+package_name);
        Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        startActivity(uninstallIntent);
    }

    private void showUninstallIndicator(){
        // get the layour
        FrameLayout uninstall_indicator;
        uninstall_indicator = (FrameLayout) rootView.findViewById(R.id.uninstall_indicator);

        // make it visible
        uninstall_indicator.setVisibility(View.VISIBLE);

        // and start the animation
        AlphaAnimation animation =  new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(500);
        uninstall_indicator.startAnimation(animation);
    }

    private void hideUninstallIndicator(){
        FrameLayout uninstall_indicator;
        uninstall_indicator = (FrameLayout)rootView.findViewById(R.id.uninstall_indicator);
        uninstall_indicator.setVisibility(View.INVISIBLE);
    }

    private void removeAppFromTab(int app_index, int tab_id){
        // retrieve tab fragment
        android.support.v4.app.FragmentManager fm = getChildFragmentManager();
        int tab_index = tHost.getCurrentTab();
        AppDrawerTabFragment fragment = (AppDrawerTabFragment)fm.findFragmentByTag(Integer.toString(tab_id));

        // remove app and refresh the tab's layout
        fragment.removeApp(app_index);
        //fragment.loadGridView();
    }

    private void dropAppInTab(String app_name) {
        // retrieve tab fragment
        android.support.v4.app.FragmentManager fm = getChildFragmentManager();
        int tab_index = tHost.getCurrentTab();
        AppDrawerTabFragment fragment = (AppDrawerTabFragment)fm.findFragmentByTag(Integer.toString(tab_index));

        // add app and refresh the tab's layout
        fragment.addApp(app_name);
        //fragment.loadGridView();
    }

    /**
     * Returns the button which the (x, y) coordinates are inside of.
     * Otherwise returns -1.
     */
    private int getHoveringButton(float x, float y) {

        // loop through all buttons and check if (x,y) is inside one of them
        for (int i = 0; i < arrButton.size(); i++){

            Button btn = arrButton.get(i);

            // get the geometry
            float high_x = btn.getX();
            float high_y = btn.getY();
            float low_x = btn.getX()+btn.getWidth();
            float low_y = btn.getY()+btn.getHeight();

            // check if (x, y) is inside
            if (x > high_x && x < low_x && y > high_y && y < low_y){
               return i;
            }
        }
        return -1;
    }

    private void setTab(int index){

        current_open_tab = index;
        tHost.setCurrentTab(index);

        // Select the relevant tab button, and unselect all the others.
        for (int i = 0; i < arrButton.size(); i++){
            if (i != index)
                arrButton.get(i).setSelected(false);
            else
                arrButton.get(index).setSelected(true);
        }
    }

    /**
     * Loads all tabs from the database.
     */
    private void loadTabs(){
        arrButton = new ArrayList<Button>();
        LinearLayout tabwidget = (LinearLayout)rootView.findViewById(R.id.custom_tabwidget);

        arrTabs = sqlhelper.getAllTabs();

        for (TabTable tab : arrTabs){
            // Create a button for each tab
            Button btn = new Button(getActivity());
            btn.setText(tab.label);
            arrButton.add(btn);

            // Set the style of the button
            btn.setBackgroundResource(R.drawable.tab_style);
            btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            btn.setTextColor(Color.WHITE);

            // Add the button to the tab widget.
            tabwidget.addView(btn);

            // create a fragment for the tab
            AppDrawerTabFragment fragment = new AppDrawerTabFragment();

            // and create a new tab
            TabHost.TabSpec tSpecFragmentid = tHost.newTabSpec(Integer.toString(tab.id-1));
            tSpecFragmentid.setIndicator(tab.label);
            tSpecFragmentid.setContent(new DummyTabContent(getActivity().getBaseContext()));
            tHost.addTab(tSpecFragmentid);

        }
    }

    private void addOnClickListener(){

        //make the tab buttons switch tab.
        for (TabTable tab : arrTabs){

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

    public int getLastTabId() {

        // if current_open_tab is already loaded, do not try to load it from preferences again.
        if (current_open_tab != -1){
            return current_open_tab;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        int lastTabId = prefs.getInt(getString(R.string.pref_last_open_tab), 0);
        return lastTabId;
    }

    /**
     * Saves the last opened tab's id in the apps preferences
     * @param tab_id
     */
    public void setLastTabId(int tab_id){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        SharedPreferences.Editor edit = prefs.edit();

        edit.putInt(getString(R.string.pref_last_open_tab), tab_id);
        edit.commit();
    }

    private void promptRenameTab(final int tabno){

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
        if (newName != null && newName.length() > 0) {
            // Update the name in the button for instant changes
            arrButton.get(tabno).setText(newName);

            // Update the name in the SQL database for the change to persist
            LauncherSQLiteHelper sql = new LauncherSQLiteHelper(getContext());
            sql.renameTab(tabno + 1, newName); // + 1 since it's not index-0 based
        }
    }
}
