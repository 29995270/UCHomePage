package com.freeze.uchomepage;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/12.
 */

public class DragTracker extends LinearLayout{

    public static float DRAG_SECTION_RATE = 0.7f;

    private List<DragActionReceiver> dragActionReceivers = new ArrayList<>();
    private ViewDragHelper dragHelper;

    private int dragDownYOffset = 0;
    private int dragDownX = 0;

    private int dragDownYMaxOffset;

    public DragTracker(Context context) {
        super(context);
    }

    public DragTracker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        dragDownYMaxOffset = Utils.dp2px(context, 224/2);

        dragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                if (dragDownYOffset >= dragDownYMaxOffset*DRAG_SECTION_RATE) {
                    dragDownYOffset += (dy/3);
                } else {
                    dragDownYOffset += dy;
                }
                return child.getTop();
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return child.getLeft();
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                onRelease(dragDownX, dragDownYOffset);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dragDownX = 0;
                        dragDownYOffset = 0;
                    }
                }, Utils.RELEASE_DURATION);

            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_MOVE) {
            dragDownX = (int) event.getX();
        }
        dragHelper.processTouchEvent(event);
        onDrag(dragDownX, dragDownYOffset);

        return true;
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

    private void onDrag(int dragDownX, int dragDownYOffset) {
        for (DragActionReceiver receiver : dragActionReceivers) {
            receiver.onDrag(dragDownX,
                    Math.min(dragDownYOffset, dragDownYMaxOffset),
                    Math.min(dragDownYOffset*1f/dragDownYMaxOffset, 1));
        }
    }

    private void onRelease(int dragDownX, int dragDownYOffset) {
        for (DragActionReceiver receiver : dragActionReceivers) {
            receiver.onRelease(dragDownX, Math.min(dragDownYOffset, dragDownYMaxOffset));
        }
    }

    public interface DragActionReceiver {
        /**
         * @param dragDownX  x-coordinate
         * @param dragDownYOffset  y offset  > 0 drag down   < 0 drag up
         * @param percent y drag percent
         */
        void onDrag(int dragDownX, int dragDownYOffset, float percent);

        void onRelease(int dragDownX, int dragDownYOffset);
    }
}
