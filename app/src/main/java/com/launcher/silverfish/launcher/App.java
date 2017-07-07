package com.launcher.silverfish.launcher;

import android.app.Application;

import com.launcher.silverfish.dbmodel.DaoMaster;
import com.launcher.silverfish.dbmodel.DaoSession;
import com.launcher.silverfish.shared.Settings;

import org.greenrobot.greendao.database.Database;

public class App extends Application {

    private Settings mSettings;
    private DaoSession mDaoSession;

    // HomeScreenFragment will set this so that PackageModifiedReceiver can notify it
    public ShortcutListener shortcutListener;

    @Override
    public void onCreate() {
        super.onCreate();

        mSettings = new Settings(this);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "settings-db", null);
        Database db = helper.getWritableDb();
        mDaoSession = new DaoMaster(db).newSession();
    }

    public Settings getSettings() {
        return mSettings;
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }
}
