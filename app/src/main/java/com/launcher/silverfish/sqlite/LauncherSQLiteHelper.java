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

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

public class LauncherSQLiteHelper {

    private DaoSession mSession;

    public LauncherSQLiteHelper(App app) {
        mSession = app.getDaoSession();
    }

    public boolean hasTabs() {
        return mSession.getTabTableDao().count() > 0;
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

    /*  Looks like this is unused. Commenting out (YAGNI).
        public List<AppTable> getAllApps() {
            return mSession.getAppTableDao().loadAll();
        }
    */
    public boolean containsApp(String activityName) {
        return mSession.getAppTableDao().queryBuilder()
                .where(AppTableDao.Properties.ActivityName.eq(activityName))
                .unique() != null;
    }

    public void addAppToTab(AppTable appTable) {
        mSession.getAppTableDao().insert(appTable);
    }

    public void addAppsToTab(List<AppTable> apps) {
        mSession.getAppTableDao().insertInTx(apps);
    }

    public void removeAppFromTab(AppTable appTable) {
        QueryBuilder qb = mSession.getAppTableDao().queryBuilder();
        AppTable app = (AppTable) qb.where(qb.and(AppTableDao.Properties.TabId.eq(appTable.getTabId()),
                AppTableDao.Properties.ActivityName.eq(appTable.getActivityName()))).unique();

        if (app != null)
            mSession.getAppTableDao().delete(app);
    }

    public void removeApps(List<AppTable> apps) {
        mSession.getAppTableDao().deleteInTx(apps);
    }

    public boolean canAddShortcut(ShortcutTable shortcutTable) {
        boolean ret = mSession.getShortcutTableDao().queryBuilder()
                .where(ShortcutTableDao.Properties.ActivityName.eq(shortcutTable.getActivityName()),
                        ShortcutTableDao.Properties.PackageName.eq(shortcutTable.getPackageName()))
                .unique() == null;
        return ret;
    }

    public long addShortcut(ShortcutTable shortcutTable) {
        shortcutTable.setId(null);
        return mSession.getShortcutTableDao().insert(shortcutTable);
    }

    public long getShortcutId(String packageName) {
        try {
            return mSession.getShortcutTableDao().queryBuilder()
                    .where(ShortcutTableDao.Properties.PackageName.eq(packageName))
                    .uniqueOrThrow().getId();
        } catch (DaoException ignored) {
            return -1;
        }
    }

    public void removeShortcut(long id) {
        try {
            mSession.getShortcutTableDao().delete(
                    mSession.getShortcutTableDao().queryBuilder()
                            .where(ShortcutTableDao.Properties.Id.eq(id))
                            .uniqueOrThrow()
            );
        } catch (DaoException ignored) { }
    }

    public List<ShortcutTable> getAllShortcuts() {
        return mSession.getShortcutTableDao().loadAll();
    }
}
