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

    private View rootView;
    private TabFragmentHandler tabHandler;

    //endregion

    //region Android lifecycle

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.activity_app_drawer, container, false);

        tabHandler = new TabFragmentHandler(getChildFragmentManager(), rootView, getActivity());

        addOnClickListener();
        setOnDragListener();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        tabHandler.loadLastOpenTab();
    }

    @Override
    public void onStop() {
        super.onStop();

        tabHandler.saveLastOpenTab();
    }

    //endregion

    //region Rename tab

    private void promptRenameTab(final TabInfo tab, final int tab_index) {

        // Find which tab we're renaming
        String tabName = tab.label;

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
                tabHandler.renameTab(tab, tab_index, input.getText().toString());
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

    //endregion

    //region Listeners

    private void addOnClickListener() {

        tabHandler.setOnTabButtonClickListener(new TabButtonClickListener(){

            @Override
            public void onClick(TabInfo tab, int position) {
                tabHandler.setTab(position);
            }

            @Override
            public boolean onLongClick(TabInfo tab, int position) {
                promptRenameTab(tab, position);
                return false;
            }
        });
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
                            int i = tabHandler.getHoveringTab(dragEvent.getX(), dragEvent.getY());

                            // If so, change to that tab
                            if (i > -1)
                                tabHandler.setTab(i);
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

                            String tab_tag = dragEvent.getClipData().getItemAt(2)
                                                                    .getText().toString();

                            // And remove it from the tab it came from
                            removeAppFromTab(app_index, tab_tag);
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

    private void removeAppFromTab(int appIndex, String tab_tag) {
        // Retrieve tab fragment
        android.support.v4.app.FragmentManager fm = getChildFragmentManager();
        AppDrawerTabFragment fragment = (AppDrawerTabFragment)
                fm.findFragmentByTag(tab_tag);

        // Remove app and refresh the tab's layout
        fragment.removeApp(appIndex);
    }

    private void dropAppInTab(String app_name) {
        // Retrieve tab fragment
        android.support.v4.app.FragmentManager fm = getChildFragmentManager();
        TabInfo tab = tabHandler.getCurrentTab();
        AppDrawerTabFragment fragment = (AppDrawerTabFragment)fm.findFragmentByTag(tab.tag);

        // Add app and refresh the tab's layout
        fragment.addApp(app_name);
        //fragment.loadGridView();
    }

    //endregion

}
