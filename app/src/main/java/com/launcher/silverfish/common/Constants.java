
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

/**
 * Class with static variables used across the application.
 */
public class Constants {
    public final static String DRAG_SHORTCUT_REMOVAL = "shortcut_removal";
    public final static String DRAG_APP_MOVE = "app_move";

    public final static String TAB_ID = "tab_id";

    public final static float DRAG_THRESHOLD_PERCENT_X = .95f;  // Percent of screen width
    public final static float MIN_DRAG_ADJ = .75f;              // Percent of icon dimension
}
