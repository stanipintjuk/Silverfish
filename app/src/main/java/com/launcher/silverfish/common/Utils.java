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

package com.launcher.silverfish.common;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by Stanislav Pintjuk on 8/3/16.
 * E-mail: stanislav.pintjuk@gmail.com
 */
public class Utils {

    /**
     * Is supplied drag event within the 'move to home page' zone?
     */
    public static boolean isBeyondRightHandThreshold(Activity a, DragEvent dragEvent) {
        int xThreshold = Utils.getScreenDimensions(a).x - Constants.SCREEN_CORNER_THRESHOLD;
        return (dragEvent.getX() > xThreshold);
    }

    public static Point getScreenDimensions(Activity activity) {
        // Get the screen size
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static boolean onBottomCenterScreenEdge(Activity activity, float x, float y) {
        Point screensize = getScreenDimensions(activity);
        int screen_width = screensize.x;
        int screen_height = screensize.y;
        // Set the threshold to be 10% of the screen height
        float thresholdx = 20.0f * screen_height / 100.0f;
        float thresholdy = 10.0f * screen_height / 100.0f;
        return (y >= screen_height - thresholdy && x <= screen_width - thresholdx && x >= 0 + thresholdx);
    }


    // 08Feb2018 Forced to rewrite as static subclass to satisfy Android Studio 3 stringencies!
    // [Refer https://stackoverflow.com/a/46166223/2376004]
    /** AsyncTask to render application icon: */
    private static class loadAppIconTask extends AsyncTask<Void, Void, Drawable> {
        private Exception exception = null;
        private WeakReference<PackageManager> pm_wr;
        private WeakReference<String> appInfo_wr;
        private WeakReference<ImageView> iv_wr;

        // Constructor
        loadAppIconTask(PackageManager pm, String appInfo, ImageView iv) {
            pm_wr = new WeakReference<>(pm);
            appInfo_wr = new WeakReference<>(appInfo);
            iv_wr = new WeakReference<>(iv);
        }

        @Override
        protected Drawable doInBackground(Void... voids) {
            // load the icon
            Drawable app_icon = null;
            try {
                PackageManager pm = pm_wr.get();
                String appInfo = appInfo_wr.get();
                if (pm==null || appInfo==null ) return null;
                app_icon = pm.getApplicationIcon(appInfo);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                exception = e;
            }
            return app_icon;
        }

        @Override
        protected void onPostExecute(Drawable app_icon) {
            ImageView iv = iv_wr.get();
            if (exception == null && app_icon != null && iv != null) {
                iv.setImageDrawable(app_icon);
                //Log.d("Utils.loadAppIconTask", "Loaded icon for '"+appInfo_wr.get()+"' OK");
            } else {
                Log.d("Utils.loadAppIconTask", "ERROR Could not load app icon.");
            }
        }
    }

    public static void loadAppIconAsync(PackageManager pm, String appInfo, ImageView iv ){
        new loadAppIconTask(pm, appInfo, iv).execute(null,null,null);
    }
}
