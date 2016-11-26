/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.launcher.silverfish;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class LauncherAppWidgetHostView extends AppWidgetHostView {

    //region Fields

    private boolean mHasPerformedLongPress;
    private CheckForLongPress mPendingCheckForLongPress;
    private LayoutInflater mInflater;

    // Store both the X and Y coordinate on touch events to determine whether the finger moved
    private float xOnDown, yOnDown;
    private float xCurrent, yCurrent;

    //endregion

    //region Constructor

    public LauncherAppWidgetHostView(Context context) {
        super(context);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Listen for touch events at this level for cancelling longClick if necessary
        setOnTouchListener(onTouchListener);
    }

    //endregion

    //region Events

    OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                // Also listen for longClick cancellation here
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_CANCEL:
                    mHasPerformedLongPress = false;
                    if (mPendingCheckForLongPress != null) {
                        removeCallbacks(mPendingCheckForLongPress);
                    }
                    break;
            }

            return true;
        }
    };

    public boolean onInterceptTouchEvent(MotionEvent ev) {

        // Consume any touch events for ourselves after long press is triggered
        if (mHasPerformedLongPress) {
            mHasPerformedLongPress = false;
            return true;
        }

        // Watch for long press events at this level to make sure
        // users can always pick up this widget
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                // Pass the location so we can later check whether the user moved the finger
                postCheckForLongClick();

                // Store the initial position
                xOnDown = ev.getX();
                yOnDown = ev.getY();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // Store the current position
                xCurrent = ev.getX();
                yCurrent = ev.getY();
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
                mHasPerformedLongPress = false;
                if (mPendingCheckForLongPress != null) {
                    removeCallbacks(mPendingCheckForLongPress);
                }
                break;
        }

        // Otherwise continue letting touch events fall through to children
        return false;
    }

    private void postCheckForLongClick() {
        mHasPerformedLongPress = false;

        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }
        mPendingCheckForLongPress.rememberWindowAttachCount();
        postDelayed(mPendingCheckForLongPress, ViewConfiguration.getLongPressTimeout());
    }

    //endregion

    //region Utils

    /** Determines whether the finger has moved under a threshold.
     * If so, we can consider the long click as valid
     * @return True if the finger is under the threshold
     */
    boolean movementUnderThreshold() {
        float xDist = Math.abs(xOnDown - xCurrent);
        float yDist = Math.abs(yOnDown - yCurrent);
        float distance = (float)Math.sqrt(xDist * xDist + yDist * yDist);
        return distance < Constants.MOVE_THRESHOLD;
    }

    //endregion

    //region Subclasses

    class CheckForLongPress implements Runnable {
        private int mOriginalWindowAttachCount;

        public void run() {
            if ((getParent() != null) && hasWindowFocus()
                    && mOriginalWindowAttachCount == getWindowAttachCount()
                    && !mHasPerformedLongPress) {

                // If the finger movement is under the threshold (i.e. didn't move)
                // and we performed a long click, notify
                if (movementUnderThreshold() && performLongClick()) {
                    mHasPerformedLongPress = true;
                }
            }
        }

        public void rememberWindowAttachCount() {
            mOriginalWindowAttachCount = getWindowAttachCount();
        }
    }

    //endregion
}
