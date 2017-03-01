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

import com.launcher.silverfish.dbmodel.AppTable;
import com.launcher.silverfish.dbmodel.AppTableDao;
import com.launcher.silverfish.dbmodel.DaoSession;
import com.launcher.silverfish.dbmodel.ShortcutTable;
import com.launcher.silverfish.dbmodel.ShortcutTableDao;
import com.launcher.silverfish.dbmodel.TabTable;
import com.launcher.silverfish.dbmodel.TabTableDao;
import com.launcher.silverfish.launcher.App;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LauncherSQLiteHelper {
    private DaoSession mSession;

    public LauncherSQLiteHelper(App app) {
        mSession = app.getDaoSession();
    }

    public TabTable addTab(String tabName) {
        TabTable newTab = new TabTable(null, tabName);
        mSession.getTabTableDao().insert(newTab);
        return newTab;
    }

    public void removeTab(long tabId) {
        mSession.getTabTableDao().deleteByKey(tabId);
    }

    public String getTabName(long tabId) {
        return mSession.getTabTableDao().queryBuilder()
                .where(TabTableDao.Properties.Id.eq(tabId))
                .uniqueOrThrow().getLabel();
    }

    public long renameTab(long tabId, String newName) {
        return mSession.insertOrReplace(new TabTable(tabId, newName));
    }

    public List<TabTable> getAllTabs() {
        return mSession.getTabTableDao().loadAll();
    }

    public List<AppTable> getAppsForTab(long tabId) {
        return mSession.getAppTableDao().queryBuilder()
                .where(AppTableDao.Properties.TabId.eq(tabId))
                .list();
    }

    public List<AppTable> getAllApps() {
        return mSession.getAppTableDao().loadAll();
    }

    public boolean containsApp(String packageName) {
        return mSession.getAppTableDao().queryBuilder()
                .where(AppTableDao.Properties.PackageName.eq(packageName))
                .unique() != null;
    }

    public void addAppToTab(String packageName, long tabId) {
        mSession.getAppTableDao().insert(new AppTable(null, packageName, tabId));
    }

    public void addAppsToTab(Map<String, Long> pkg_categoryId) {
        List<AppTable> apps = new LinkedList<>();
        for (Map.Entry<String, Long> entry : pkg_categoryId.entrySet()) {
            apps.add(new AppTable(null, entry.getKey(), entry.getValue()));
        }
        mSession.getAppTableDao().insertInTx(apps);
    }

    public void removeAppFromTab(String packageName, long tabId) {
        QueryBuilder qb = mSession.getAppTableDao().queryBuilder();
        AppTable app = (AppTable)qb.where(qb.and(AppTableDao.Properties.TabId.eq(tabId),
                AppTableDao.Properties.PackageName.eq(packageName))).unique();

        if (app != null)
            mSession.getAppTableDao().delete(app);
    }

    public void removeApps(List<AppTable> apps) {
        mSession.getAppTableDao().deleteInTx(apps);
    }

    public long addShortcut(String packageName) {
        return mSession.getShortcutTableDao().insert(new ShortcutTable(null, packageName));
    }

    public void removeShortcut(long id) {
        ShortcutTable app = mSession.getShortcutTableDao().queryBuilder()
                .where(ShortcutTableDao.Properties.Id.eq(id))
                .unique();

        if (app != null)
            mSession.getShortcutTableDao().delete(app);
    }

    public List<ShortcutTable> getAllShortcuts() {
        return mSession.getShortcutTableDao().loadAll();
    }
}
