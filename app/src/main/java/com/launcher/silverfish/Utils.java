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
import android.graphics.Point;
import android.view.Display;

/**
 * Created by Stanislav Pintjuk on 8/3/16.
 * E-mail: stanislav.pintjuk@gmail.com
 */
public class Utils {

    public static Point getScreenDimensions(Activity activity){
        // Get the screen size
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static boolean onBottomScreenEdge(Activity activity, float y){
        int screen_height = getScreenDimensions(activity).y;
        // if on bottom 10% of the screen then consider it to be on corner
        float threshold = 10.0f*screen_height/100.0f;

        return (y >= screen_height - threshold);
    }
}
