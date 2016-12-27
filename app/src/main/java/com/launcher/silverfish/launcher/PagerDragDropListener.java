package com.launcher.silverfish.launcher;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.launcher.silverfish.R;

/**
 * Created by stani on 2016-12-27.
 */

public class PagerDragDropListener {
    private final DragDirectionCallback mCallback;

    public PagerDragDropListener(final View rootView, final DragDirectionCallback callback) {
        mCallback = callback;
        View topLeft = rootView.findViewById(R.id.pager_drag_layout_tl);
        View topMid = rootView.findViewById(R.id.pager_drag_layout_tm);
        View topRight = rootView.findViewById(R.id.pager_drag_layout_tr);
        View midLeft = rootView.findViewById(R.id.pager_drag_layout_ml);
        View midMid = rootView.findViewById(R.id.pager_drag_layout_mm);
        View midRight = rootView.findViewById(R.id.pager_drag_layout_mr);
        View botLeft = rootView.findViewById(R.id.pager_drag_layout_bl);
        View botMid = rootView.findViewById(R.id.pager_drag_layout_bm);
        View botRight = rootView.findViewById(R.id.pager_drag_layout_br);

        midRight.setOnDragListener(new RightDragDetector(midRight));
        midLeft.setOnDragListener(new LeftDragDetector(midLeft));
    }

    private void draggedToLeft() {
        mCallback.draggedLeft();
    }

    private void draggedToRight() {
        mCallback.draggedRight();
    }

    private class RightDragDetector extends DragDetector {

        public RightDragDetector(View view) {
            super(view);
        }

        @Override
        protected void dragExitedCallback(ClipDescription clipDescription, Object localState) {

        }

        @Override
        protected void dragEnteredCallback(ClipDescription clipDescription, Object localState, float x, float y) {
            draggedToRight();
        }
    }

    private class LeftDragDetector extends DragDetector {

        public LeftDragDetector(View view) {
            super(view);
        }

        @Override
        protected void dragExitedCallback(ClipDescription clipDescription, Object localState) {

        }

        @Override
        protected void dragEnteredCallback(ClipDescription clipDescription, Object localState, float x, float y) {
            draggedToLeft();
        }
    }

    private abstract class DragDetector extends DragAndDropBase {
        final private View mView;
        final private Drawable mHLBackground;
        final private Drawable mDefaultBackground;

        public DragDetector(final View view) {
            this(view, new ColorDrawable(Color.RED));
        }

        public DragDetector(final View view, final Drawable highlightBackground) {
            this(view, highlightBackground, new ColorDrawable(Color.TRANSPARENT));
        }

        public DragDetector(final View view, final Drawable highlightBackground, final Drawable defaultBackground) {
            mView = view;
            mHLBackground = highlightBackground;
            mDefaultBackground = defaultBackground;

        }

        @Override
        final protected boolean dragEnded(ClipDescription clipDescription, Object localState) {
            return true;
        }

        @Override
        final protected boolean drop(ClipDescription clipDescription, Object localState, float x, float y, ClipData clipData) {
            return true;
        }

        @Override
        final protected boolean dragExited(ClipDescription clipDescription, Object localState) {
            mView.setBackground(mDefaultBackground);
            dragExitedCallback(clipDescription, localState);
            return true;
        }


        @Override
        final protected boolean dragLocation(ClipDescription clipDescription, Object localState, float x, float y) {
            return true;
        }

        @Override
        final protected boolean dragEntered(ClipDescription clipDescription, Object localState, float x, float y) {
            mView.setBackground(mHLBackground);
            dragEnteredCallback(clipDescription, localState, x, y);
            return true;
        }

        @Override
        final protected boolean dragStarted(ClipDescription clipDescription, Object localState, float x, float y) {
            return true;
        }

        protected abstract void dragExitedCallback(ClipDescription clipDescription, Object localState);
        protected abstract void dragEnteredCallback(ClipDescription clipDescription, Object localState, float x, float y);
    }

    interface DragDirectionCallback {
        void draggedLeft();
        void draggedRight();
    }
}
