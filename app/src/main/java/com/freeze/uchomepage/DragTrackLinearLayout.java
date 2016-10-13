package com.freeze.uchomepage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/12.
 */

public class DragTrackLinearLayout extends LinearLayout {

    private List<DragActionReceiver> dragActionReceivers = new ArrayList<>();

    public DragTrackLinearLayout(Context context) {
        super(context);
    }

    public DragTrackLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                break;
        }

        return super.onTouchEvent(event);
    }

    public void addDragActionReceiver(DragActionReceiver receiver) {
        if (!dragActionReceivers.contains(receiver)) {
            dragActionReceivers.add(receiver);
        }
    }

    public void removeDragActionReceiver(DragActionReceiver receiver) {
        dragActionReceivers.remove(receiver);
    }

    @Override
    protected void onDetachedFromWindow() {
        dragActionReceivers.clear();
        super.onDetachedFromWindow();
    }

    public interface DragActionReceiver {
        /**
         * @param dragDownX  x-coordinate
         * @param dragDownYOffset  y offset  > 0 drag down   < 0 drag up
         */
        void onDrag(int dragDownX, int dragDownYOffset);

        void onRelease(int dragDownX, int dragDownYOffset);
    }
}
