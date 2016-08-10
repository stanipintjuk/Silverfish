
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

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.launcher.silverfish.sqlite.LauncherSQLiteHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class AppDrawerTabFragment extends Fragment {

    //region Fields

    LauncherSQLiteHelper sqlHelper;

    private View rootView;

    private PackageManager mPacMan;
    private List<AppDetail> appsList;
    private GridView appsView;
    private ArrayAdapter<AppDetail> arrayAdapter;

    private int tabId;

    //endregion

    //region Android lifecycle

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // get the sql database helper and the view of this tab
        sqlHelper = new LauncherSQLiteHelper(getActivity().getBaseContext());
        rootView = inflater.inflate(R.layout.activity_app_drawer_tab, container, false);

        // Get this tab's ID.
        Bundle args = getArguments();
        tabId = args.getInt(Constants.TAB_ID);
        appsView = (GridView) rootView.findViewById(R.id.apps_grid);

        mPacMan = getActivity().getPackageManager();
        appsList = new ArrayList<AppDetail>();

        // Load the apps and update the view
        loadApps();
        loadGridView();

        return rootView;
    }

    //endregion

    //region App management

    //region Add app

    public void addApp(String app_name) {
        boolean success = addAppToArrayAdapter(app_name);
        if (tabId != 0 && success)
            sqlHelper.addAppToTab(app_name, tabId + 1);
    }

    private boolean addAppToArrayAdapter(String app_name) {
        try {
            // Get the information about the app
            ApplicationInfo appInfo = mPacMan.getApplicationInfo(app_name,PackageManager.GET_META_DATA);
            AppDetail appDetail = new AppDetail();

            // And add it to the array adapter.
            appDetail.label = mPacMan.getApplicationLabel(appInfo);
            appDetail.icon = mPacMan.getApplicationIcon(appInfo);
            appDetail.name = app_name;
            arrayAdapter.add(appDetail);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(getActivity().getBaseContext(),
                    "Error: Could not retrieve information about the app",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean addAppToList(String app_name) {
        try {
            // Get the information about the app
            ApplicationInfo appInfo = mPacMan.getApplicationInfo(app_name,PackageManager.GET_META_DATA);
            AppDetail appDetail = new AppDetail();

            // And add it to the list.
            appDetail.label = mPacMan.getApplicationLabel(appInfo);
            appDetail.icon = mPacMan.getApplicationIcon(appInfo);
            appDetail.name = app_name;
            appsList.add(appDetail);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    //endregion

    //region Remove app

    public void removeApp(int appIndex) {
        if (tabId != 0)
            sqlHelper.removeAppFromTab(appsList.get(appIndex).name.toString(), tabId +1);

        arrayAdapter.remove(appsList.get(appIndex));
        //appsList.remove(appIndex);
    }

    //endregion

    //region Load apps

    /**
     * Loads apps from the database
     */
    private void loadApps() {

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        switch (tabId) {
            case 0:
                // Tab 0 is a special tab and includes all except for the once in other tabs
                // so we retrieve all apps that are in the database
                LinkedList<String> ordered_apps = sqlHelper.getAllApps();

                // And all installed apps on the device
                List<ResolveInfo> availableActivities = mPacMan.queryIntentActivities(i, 0);
                
                // And only add those that are not in the database
                for (int j = 0; j < availableActivities.size(); j++)    {
                    ResolveInfo ri = availableActivities.get(j);

                    // Skip the apps that are in the database
                    if (ordered_apps.contains(ri.activityInfo.packageName)) {
                        continue;
                    }

                    AppDetail app = new AppDetail();
                    app.label = ri.loadLabel(mPacMan);
                    app.name = ri.activityInfo.packageName;

                    // Load the icon later in an async task.
                    app.icon = null;

                    appsList.add(app);
                }
                break;
            default:
                // All other tabs just query the apps from the database
                LinkedList<String> app_names = sqlHelper.getAppsForTab(tabId +1);
                for (String app_name : app_names) {

                    boolean success = addAppToList(app_name);
                    // If the app could not be added then it was probably uninstalled,
                    // so we have to remove it from the database
                    if (!success) {
                        Log.d("DB", "Removing app "+app_name+" from db");
                        sqlHelper.removeAppFromTab(app_name, this.tabId +1);
                    }
                }
        }
    }

    //endregion

    //endregion

    //region UI

    private void loadGridView() {

        // First sort the apps list
        Collections.sort(appsList, new Comparator<AppDetail>() {
            @Override
            public int compare(AppDetail app1, AppDetail app2) {
                return app1.label.toString().compareTo(app2.label.toString());
            }
        });

        // Create the array adapter
        arrayAdapter = new ArrayAdapter<AppDetail>(getActivity(),
                R.layout.list_item,
                appsList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                AppDetail app = appsList.get(position);
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item, null);
                }

                // Set the application icon and label for this view

                // load the app icon in an async task
                ImageView appIcon = (ImageView) convertView.findViewById(R.id.item_app_icon);
                Utils.loadAppIconAsync(mPacMan, app.name.toString(), appIcon);

                TextView appLabel = (TextView) convertView.findViewById(R.id.item_app_label);
                appLabel.setText(app.label);

                // Set various click and touch listeners
                setClickListeners(convertView, app.name.toString(), position);

                return convertView;
            }
        };
        // Add the array adapter
        appsView.setAdapter(arrayAdapter);
    }

    //endregion

    //region Listeners

    @SuppressWarnings("deprecation")
    private void setClickListeners(View view, final String appName, final int appIndex) {

        // Start a drag action when icon is long clicked
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                // Add data to the clipboard
                String[] mime_type = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData data = new ClipData(Constants.DRAG_APP_MOVE, mime_type, new ClipData.Item(appName));
                data.addItem(new ClipData.Item(Integer.toString(appIndex)));
                data.addItem(new ClipData.Item(Integer.toString(tabId)));

                // The drag shadow is simply the app's  icon
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        view.findViewById(R.id.item_app_icon));

                // "This method was deprecated in API level 24. Use startDragAndDrop()
                // for newer platform versions."
                if (Build.VERSION.SDK_INT < 24) {
                    view.startDrag(data, shadowBuilder, view, 0);
                } else {
                    view.startDragAndDrop(data, shadowBuilder, view, 0);
                }
                return true;

            }
        });

        // Start the app activity when icon is clicked.
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = mPacMan.getLaunchIntentForPackage(appName);
                startActivity(i);
            }
        });
    }

    //endregion
}
