package com.launcher.silverfish.launcher;

import android.content.ClipData;
import android.content.ClipDescription;
import android.view.DragEvent;
import android.view.View;

/**
 * Created by stani on 2016-12-27.
 */

abstract public class DragAndDropBase implements View.OnDragListener {
    @Override
    final public boolean onDrag(final View view, final DragEvent dragEvent) {
        switch(dragEvent.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                return dragStarted(dragEvent.getClipDescription(),
                        dragEvent.getLocalState(),
                        dragEvent.getX(),
                        dragEvent.getY());
            case DragEvent.ACTION_DRAG_ENTERED:
                return dragEntered(dragEvent.getClipDescription(),
                        dragEvent.getLocalState(),
                        dragEvent.getX(),
                        dragEvent.getY());
            case DragEvent.ACTION_DRAG_LOCATION:
                return dragLocation(dragEvent.getClipDescription(),
                        dragEvent.getLocalState(),
                        dragEvent.getX(),
                        dragEvent.getY());
            case DragEvent.ACTION_DRAG_EXITED:
                return dragExited(dragEvent.getClipDescription(),
                        dragEvent.getLocalState());
            case DragEvent.ACTION_DROP:
                return drop(dragEvent.getClipDescription(),
                        dragEvent.getLocalState(),
                        dragEvent.getX(),
                        dragEvent.getY(),
                        dragEvent.getClipData());
            case DragEvent.ACTION_DRAG_ENDED:
                return dragEnded(dragEvent.getClipDescription(),
                        dragEvent.getLocalState());
            default:
                throw new UnknownDragEventException(dragEvent);
        }
    }

    protected abstract boolean dragEnded(ClipDescription clipDescription, Object localState);

    protected abstract boolean drop(ClipDescription clipDescription, Object localState, float x, float y, ClipData clipData);

    protected abstract boolean dragExited(ClipDescription clipDescription, Object localState);

    protected abstract boolean dragLocation(ClipDescription clipDescription, Object localState, float x, float y);

    protected abstract boolean dragEntered(ClipDescription clipDescription, Object localState, float x, float y);

    protected abstract boolean dragStarted(ClipDescription clipDescription, Object localState, float x, float y);
}

class UnknownDragEventException extends RuntimeException {
    public UnknownDragEventException(DragEvent dragEvent) {
       super("Unkown drag event: " + dragEvent.getAction());
    }
}
