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

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;

/**
 * Created by Stanislav Pintjuk on 8/3/16.
 * E-mail: stanislav.pintjuk@gmail.com
 */
public class Utils {

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
        float thresholdx = 20.0f*screen_height/100.0f;
        float thresholdy = 10.0f*screen_height/100.0f;
        return (y >= screen_height - thresholdy && x <= screen_width - thresholdx && x >= 0+thresholdx);
    }

    public static void loadAppIconAsync(final PackageManager pm, final String appInfo, final ImageView im ){

        // Create an async task
        AsyncTask<Void,Void,Drawable> loadAppIconTask = new AsyncTask<Void, Void, Drawable>() {

            // Keep track of all the exceptions
            private Exception exception = null;


            @Override
            protected Drawable doInBackground(Void... voids) {
                // load the icon
                Drawable app_icon = null;
                try {
                    app_icon = pm.getApplicationIcon(appInfo);

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    exception = e;
                }

                return app_icon;
            }

            @Override
            protected void onPostExecute(Drawable app_icon){
                if (exception == null) {
                    im.setImageDrawable(app_icon);

                } else {
                    Log.d("Utils.loadAppIconAsync", "ERROR Could not load app icon.");

                }
            }
        };

        loadAppIconTask.execute(null,null,null);
    }
}
