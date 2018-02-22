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

package com.launcher.silverfish.launcher.appdrawer;

import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.InputType;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.launcher.silverfish.R;
import com.launcher.silverfish.common.Constants;
import com.launcher.silverfish.common.Utils;
import com.launcher.silverfish.dbmodel.AppTable;
import com.launcher.silverfish.launcher.LauncherActivity;
import com.launcher.silverfish.models.TabInfo;

public class TabbedAppDrawerFragment extends Fragment {

    //region Fields

    private View rootView;
    private TabFragmentHandler tabHandler;

    // Are we swapping tabs? This will be consumed by the tab.onClick event
    private boolean isSwappingTabs;
    private TabInfo swappingTab;
    private int swappingTabIndex = -1;

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

    //region Tab options: rename, remove, add

    private void promptTabOptions(final TabInfo tab, final int tab_index) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Set up tab options
        CharSequence[] options = new CharSequence[]{
                getString(R.string.text_rename),
                getString(R.string.text_move_tab),
                getString(R.string.text_remove),
                getString(R.string.text_add_tab)
        };

        // add click listener
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        promptRenameTab(tab, tab_index);
                        break;
                    case 1:
                        prepareMoveTab(tab, tab_index);
                        break;
                    case 2:
                        removeTab(tab, tab_index);
                        break;
                    case 3:
                        promptNewTab();
                        break;
                }
            }
        });

        builder.show();
    }

    private void promptNewTab() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Set up the dialog
        builder.setTitle(getString(R.string.text_new_tab_name));

        // Set up the input
        final EditText input = new EditText(getContext());
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // setup the buttons
        builder.setPositiveButton(getString(R.string.text_add_tab), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    tabHandler.addTab(input.getText().toString());
                } catch (IllegalArgumentException e) {
                    // This means that the tab name was empty.
                    showToast(R.string.text_cannot_name_empty);
                }
            }
        });
        builder.setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void removeTab(TabInfo tab, int tab_index) {
        try {
            tabHandler.removeTab(tab, tab_index);
        } catch (IllegalArgumentException e) {
            // This means that the user wanted to remove the first tab
            showToast(R.string.text_cannot_remove_tab);
        }
    }

    private void promptRenameTab(final TabInfo tab, final int tab_index) {

        // Find which tab we're renaming
        String tabName = tab.getLabel();

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
                try {
                    tabHandler.renameTab(tab, tab_index, input.getText().toString());
                } catch (IllegalArgumentException e){
                    /* This means that the user entered an empty name */
                    showToast(R.string.text_cannot_name_empty);
                }
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

    private void prepareMoveTab(final TabInfo tab, final int tabIndex) {
        isSwappingTabs = true;
        swappingTab = tab;
        swappingTabIndex = tabIndex;
        showLongToast(R.string.text_move_tab_toast);
    }

    private void endMoveTab(final TabInfo tab, final int tabIndex) {
        if (swappingTabIndex != tabIndex) {
            try {
                tabHandler.swapTabs(swappingTab, swappingTabIndex, tab, tabIndex);
            } catch (IllegalArgumentException e) {
                // This means that the user wanted to remove the first tab
                showToast(R.string.text_cannot_move_tab);
            }
        } else {
            showToast(R.string.text_operation_cancelled);
        }
        consumeMoveTab();
    }

    private void consumeMoveTab() {
        isSwappingTabs = false;
        swappingTab = null;
        swappingTabIndex = -1;
    }

    //endregion

    //region Listeners

    private void addOnClickListener() {

        tabHandler.setOnTabButtonClickListener(new AppDrawerTabFragment.TabButtonClickListener(){

            @Override
            public void onClick(TabInfo tab, int position) {
                if (isSwappingTabs) {
                    endMoveTab(tab, position);
                }
                // Don't add an else clause, so we also update the tab position to refresh the apps
                tabHandler.setTab(position);
            }

            @Override
            public boolean onLongClick(TabInfo tab, int position) {
                promptTabOptions(tab, position);
                return false;
            }
        });
    }

    private void setOnDragListener() {

        rootView.setOnDragListener(new View.OnDragListener() {

            private float dragBeginX, dragOffsetX, dragBeginY, dragOffsetY;
            boolean hasTabHoverOccurred;

            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                String cdLabel = Utils.getClipLabel(dragEvent, "TappedAppDrawerFragment");

                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED: {
                        dragBeginX = dragEvent.getX();  // Starting position (x)
                        dragBeginY = dragEvent.getY();  // Starting position (y)
                        hasTabHoverOccurred = false;

                        // Check ClipDescription label for expected value
                        if (!cdLabel.equals(Constants.DRAG_APP_MOVE)) {
                            return false;
                        }

                        // Show the uninstall indicator
                        showUninstallIndicator();
                        break;
                    }

                    case DragEvent.ACTION_DRAG_ENTERED: {
                        // Don't do anything
                        break;
                    }

                    case DragEvent.ACTION_DRAG_LOCATION: {
                        // Do nothing if inside 'Move to Home Page' zone
                        if (!Utils.isBeyondRightHandThreshold(getActivity(), dragEvent)) {
                             // Check if the drag is hovering over a tab button
                             int i = tabHandler.getHoveringTab(dragEvent.getX(), dragEvent.getY());
                             // If so, change to that tab
                             if (i > -1) {
                                 hasTabHoverOccurred = true;
                                 tabHandler.setTab(i);
                             }
                         }
                        break;
                    }

                    case DragEvent.ACTION_DROP: {
                        dragOffsetX = dragBeginX - dragEvent.getX();    // Total x movement (+/-)
                        dragOffsetY = dragBeginY - dragEvent.getY();    // Total y movement (+/-)
                        int absDragPx = (int) Math.max(Math.abs(dragOffsetX), Math.abs(dragOffsetY));
                        int absIconPx = Utils.getIconDimPixels(getContext());

                        AppTable appTable = new AppTable();
                        appTable.setPackageName(dragEvent.getClipData().getItemAt(0).getText().toString());
                        appTable.setActivityName(dragEvent.getClipData().getItemAt(1).getText().toString());

                        // If app is dropped on the uninstall indicator uninstall the app
                        if (Utils.onBottomCenterScreenEdge(getActivity(), dragEvent.getX(), dragEvent.getY())) {
                            launchUninstallIntent(appTable.getPackageName());
                        }
                        // Add shortcut to home page if dragged to far right
                        else if (Utils.isBeyondRightHandThreshold(getActivity(), dragEvent)) {
                            moveToHomeScreen((LauncherActivity)getActivity(), appTable);
                        }
                        // Transfer app to another folder
                        else if (hasTabHoverOccurred) {
                            // TODO Drop!!!!!!!
                            int appIndex = Integer.parseInt(
                                    dragEvent.getClipData().getItemAt(2).getText().toString());
                            String tabTag = dragEvent.getClipData().getItemAt(3).getText().toString();
                            removeAppFromTab(appIndex, tabTag);
                            dropAppInTab(appTable);
                        }
                        // Display submenu if 'minimal drag'
                        else if (absDragPx < Constants.MIN_DRAG_ADJ * absIconPx) {
                            showExtraOptionsMenu(appTable);
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

    private void dropAppInTab(AppTable appTable) {
        // Retrieve tab fragment
        android.support.v4.app.FragmentManager fm = getChildFragmentManager();
        TabInfo tab = tabHandler.getCurrentTab();
        AppDrawerTabFragment fragment = (AppDrawerTabFragment)fm.findFragmentByTag(tab.getTag());

        // Add app and refresh the tab's layout
        fragment.addApp(appTable);
    }

    //endregion

    //region Extra options per app

    private void showExtraOptionsMenu(final AppTable appTable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Set up extra menu options
        CharSequence[] options = new CharSequence[]{
                getString(R.string.add_to_home_screen)
        };

        // Add click listener
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        moveToHomeScreen((LauncherActivity)getActivity(), appTable);
                        break;
                }
            }
        });

        builder.show();
    }

    private void moveToHomeScreen(LauncherActivity activity, AppTable appName) {
        activity.addShortcut(appName);
        activity.moveToScreen(1);
    }

    //endregion

    //region UI

    private void showToast(int stringId) {
        Toast.makeText(getContext(), getString(stringId), Toast.LENGTH_SHORT).show();
    }

    private void showLongToast(int stringId) {
        Toast.makeText(getContext(), getString(stringId), Toast.LENGTH_LONG).show();
    }

    //endregion

}
