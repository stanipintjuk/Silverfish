
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

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.launcher.silverfish.R;
import com.launcher.silverfish.common.Constants;
import com.launcher.silverfish.dbmodel.AppTable;
import com.launcher.silverfish.launcher.App;
import com.launcher.silverfish.models.AppDetail;
import com.launcher.silverfish.models.TabInfo;
import com.launcher.silverfish.shared.Settings;
import com.launcher.silverfish.sqlite.LauncherSQLiteHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppDrawerTabFragment extends Fragment {

    //region Members

    LauncherSQLiteHelper sqlHelper;
    Settings settings;

    private View rootView;
    private TextView emptyCategoryTextView;

    private PackageManager mPacMan;
    private List<AppDetail> appsList;
    private GridView appsView;
    private ArrayAdapter<AppDetail> arrayAdapter;

    private long tabId;

    //endregion

    //region Initialization

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // get the sql database helper and the view of this tab
        sqlHelper = new LauncherSQLiteHelper((App)getActivity().getApplication());
        settings = new Settings(getContext());

        rootView = inflater.inflate(R.layout.activity_app_drawer_tab, container, false);
        emptyCategoryTextView = (TextView)rootView.findViewById(R.id.textView_empty_category_notice);

        // Get this tab's ID.
        Bundle args = getArguments();
        tabId = args.getLong(Constants.TAB_ID);
        appsView = (GridView) rootView.findViewById(R.id.apps_grid);

        mPacMan = getActivity().getPackageManager();
        appsList = new ArrayList<>();

        setColors(settings.getDrawerBgColor(), settings.getFontFgColor());

        // Load the apps and update the view
        loadApps();
        loadGridView();
        return rootView;
    }

    //endregion

    //region Applications

    //region Adding applications

    public void addApp(AppTable appTable) {
        boolean success = addAppToList(appTable);
        if (success) {
            sortAppsList();
            arrayAdapter.notifyDataSetChanged();
            // add to database only if it is not the first tab
            if (tabId != 1)
                sqlHelper.addAppToTab(appTable, tabId);
        }
    }

    private boolean addAppToList(AppTable appTable) {
        try {
            // Get the information about the app
            ApplicationInfo appInfo = mPacMan.getApplicationInfo(
                    appTable.getPackageName(), PackageManager.GET_META_DATA);
            AppDetail appDetail = new AppDetail();

            // And add it to the list.
            // TODO: Possible source of bug! Doesn't take into account activityName when getting label.
            appDetail.label = mPacMan.getApplicationLabel(appInfo);
            appDetail.icon = null; // Loaded later by AppArrayAdapter
            appDetail.packageName = appTable.getPackageName();
            appDetail.activityName = appTable.getActivityName();
            appsList.add(appDetail);

            hideEmptyCategoryNotice();
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    //endregion

    //region Removing applications

    public void removeApp(int appIndex) {
        if (tabId != 1)
            // TODO: Use AppTable directly instead of AppDetail?
            sqlHelper.removeAppFromTab(
                    new AppTable(null,
                            appsList.get(appIndex).packageName.toString(),
                            appsList.get(appIndex).activityName.toString(), tabId)
            );

        arrayAdapter.remove(appsList.get(appIndex));

        // show empty categry notice if last app was removed
        if (appsList.size() == 0) {
            showEmptyCategoryNotice();
        }
    }

    //endregion

    //region Loading applications

    /**
     * Loads apps from the database
     */
    private void loadApps() {

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        switch ((int)tabId) {
            case 1:
                // Tab 1 is a special tab and includes all except for the ones in other tabs
                // Retrieve all installed apps on the device
                List<ResolveInfo> availableActivities = mPacMan.queryIntentActivities(i, 0);
                
                // And only add those that are not in the database
                for (int j = 0; j < availableActivities.size(); j++)    {
                    ResolveInfo ri = availableActivities.get(j);

                    if (sqlHelper.containsApp(ri.activityInfo.packageName))
                        continue;

                    AppDetail app = new AppDetail();
                    app.label = ri.loadLabel(mPacMan);
                    app.packageName = ri.activityInfo.packageName;
                    app.activityName = ri.activityInfo.name;


                    // Load the icon later in an async task.
                    app.icon = null;

                    appsList.add(app);
                }
                break;
            default:
                // All other tabs just query the apps from the database
                List<AppTable> apps = sqlHelper.getAppsForTab(tabId);
                for (AppTable app : apps) {

                    boolean success = addAppToList(app);
                    // If the app could not be added then it was probably uninstalled,
                    // so we have to remove it from the database
                    if (!success) {
                        Log.d("DB", "Removing app "+app.getPackageName()+" from db");
                        sqlHelper.removeAppFromTab(app);
                    }
                }

                // show the empty category notice if this tab is empty
                if (apps.size() == 0) {
                    showEmptyCategoryNotice();
                }
        }
    }

    //endregion

    //endregion

    //region UI

    private void setColors(int background, int foreground) {
        rootView.setBackgroundColor(background);
        emptyCategoryTextView.setTextColor(foreground);
    }

    private void showEmptyCategoryNotice() {
        emptyCategoryTextView.setVisibility(View.VISIBLE);
    }
    private void hideEmptyCategoryNotice() {
        emptyCategoryTextView.setVisibility(View.GONE);
    }

    private void loadGridView() {
        // First sort the apps list
        sortAppsList();

        // Create the array adapter
        arrayAdapter = new AppArrayAdapter(getActivity(), R.layout.list_item, appsList, getTag());
        appsView.setAdapter(arrayAdapter);
    }

    private void sortAppsList(){
        Collections.sort(appsList, new Comparator<AppDetail>() {
            @Override
            public int compare(AppDetail app1, AppDetail app2) {
                return app1.label.toString().compareTo(app2.label.toString());
            }
        });
    }

    /**
     * Created by Stanislav Pintjuk on 8/12/16.
     * E-mail: stanislav.pintjuk@gmail.com
     */
    public interface TabButtonClickListener {
        void onClick(TabInfo tab, int position);
        boolean onLongClick(TabInfo tab, int position);
    }
}
