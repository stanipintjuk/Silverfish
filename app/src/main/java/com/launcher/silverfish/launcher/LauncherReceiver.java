package com.launcher.silverfish.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.launcher.silverfish.dbmodel.ShortcutTable;
import com.launcher.silverfish.sqlite.LauncherSQLiteHelper;

public class LauncherReceiver extends BroadcastReceiver {

    private static final String INSTALL_SHORTCUT =
            "com.android.launcher.action.INSTALL_SHORTCUT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(INSTALL_SHORTCUT)) {
            Intent target = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
            if (target == null)
                return; // No target, do nothing

            // TODO Make use of:
            //  intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
            //    nullable string
            //  intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
            //    nullable icon, maybe Intent.ShortcutIconResource (beware ClassCastException)
            //
            // TODO Save 'target.toUri(0)' instead, to preserve all the information
            final LauncherSQLiteHelper sql =
                    new LauncherSQLiteHelper((App)context.getApplicationContext());
            // TODO: IMPORTANT! canAddShortcut uses activityName field to search.
            //       Will cause NullPointerException.
            // TODO: IMPORTANT! Figure out how to get activityName from here.
            ShortcutTable shortcutTable = new ShortcutTable(null, target.getPackage(), null);
            if (sql.canAddShortcut(shortcutTable))
                sql.addShortcut(shortcutTable);
        }
    }
}
