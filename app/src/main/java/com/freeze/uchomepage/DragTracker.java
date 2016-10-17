package com.freeze.uchomepage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/12.
 */

public class DragTracker extends LinearLayout{

    private static final int RELEASE_STATE_INIT = 0;
    private static final int RELEASE_STATE_UP = 1;
    private static final int RELEASE_STATE_DOWN = 2;

    public static float DRAG_SECTION_RATE = 0.7f;

    private List<DragActionReceiver> dragActionReceivers = new ArrayList<>();
    private ViewDragHelper dragHelper;

    private int dragYOffset = 0;

    private int dragDownX = 0;
    private ViewConfiguration viewConfiguration;
    private boolean animating;
    private ValueAnimator releaseAnim;

    private int dragUpThreshold = Utils.dp2px(getContext(), -DragUpReceiver.MAX_DRAG_UP_DIS);
    private int dragDownThreshold = (int) (Utils.dp2px(getContext(), 224/2));

    private OvershootInterpolator overshootInterpolator = new OvershootInterpolator();
    private AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();

    private int releaseState = 0;

    public DragTracker(Context context) {
        super(context);
    }

    public DragTracker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        viewConfiguration = ViewConfiguration.get(context);

        dragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return viewConfiguration.getScaledTouchSlop();
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                if (dragYOffset >= dragDownThreshold*DRAG_SECTION_RATE) {
                    dragYOffset += (dy/3);
                } else {
                    dragYOffset += dy;
                }
                if (dragYOffset > dragDownThreshold) {
                    dragYOffset = dragDownThreshold;
                } else if (dragYOffset < dragUpThreshold) {
                    dragYOffset = dragUpThreshold;
                }
                return child.getTop();
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return child.getLeft();
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                onRelease(dragDownX, dragYOffset);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dragDownX = 0;
                    }
                }, Utils.RELEASE_DURATION);

            }
        });

        releaseAnim = ValueAnimator.ofInt();
        releaseAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                if (value < 0 && dragYOffset > 0 || value > 0 && dragYOffset < 0) {  //跨越边界时的处理
                    onDrag(dragDownX, 0);
                }
                onDrag(dragDownX, value);

            }
        });
        releaseAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animating = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                animating = true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev) || dragYOffset != 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_MOVE) {
            dragDownX = (int) event.getX();
        }
        dragHelper.processTouchEvent(event);
        onDrag(dragDownX, dragYOffset);

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

    public void initLocation() {
        onDrag(0, 0);
        dragDownX = 0;
        dragYOffset = 0;
    }

    public void newsLocation() {
        if (dragYOffset > 0) {
            initLocation();  //for downReceiver
        }
        onDrag(0, dragUpThreshold);  // for upReceiver
        dragDownX = 0;
        dragYOffset = dragUpThreshold;
    }

    public void animationRelease(final Runnable runnable) {
        if (dragYOffset < dragUpThreshold /2) {
            //drag up to news
            int offset = Math.max(dragUpThreshold, dragYOffset);
            releaseAnim.setIntValues(offset, dragUpThreshold);
            releaseAnim.setInterpolator(accelerateInterpolator);
            releaseAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animation.removeListener(this);
                    runnable.run();
                }
            });
            releaseAnim.start();
        } else if (dragYOffset >= dragUpThreshold /2 && dragYOffset < 0) {
            //drag up re init
            releaseAnim.setIntValues(dragYOffset, 0);
            releaseAnim.setInterpolator(accelerateInterpolator);
            releaseAnim.start();
        } else if (dragYOffset < dragDownThreshold * DRAG_SECTION_RATE) {
            //drag down re init
            releaseAnim.setIntValues(dragYOffset, 0);
            releaseAnim.start();
        } else {
            //drag down to news
            releaseAnim.setIntValues(dragYOffset, 0, dragUpThreshold);
            releaseAnim.setInterpolator(accelerateInterpolator);
            releaseAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animation.removeListener(this);
                    runnable.run();
                }
            });
            releaseAnim.start();
        }


    }

    @Override
    protected void onDetachedFromWindow() {
        dragActionReceivers.clear();
        super.onDetachedFromWindow();
    }

    private void onDrag(int dragDownX, int dragDownYOffset) {
        dragYOffset = dragDownYOffset;
        for (DragActionReceiver receiver : dragActionReceivers) {
            receiver.onDrag(dragDownX,
                    dragDownYOffset,
                    Math.min(dragDownYOffset *1f/dragDownThreshold, 1));
        }
    }

    private void onRelease(int dragDownX, int dragDownYOffset) {
        for (DragActionReceiver receiver : dragActionReceivers) {
            receiver.onRelease(dragDownX, Math.min(dragDownYOffset, dragDownThreshold));
        }
    }

    public interface DragActionReceiver {
        /**
         * @param dragDownX  x-coordinate
         * @param dragDownYOffset  y offset  > 0 drag down   < 0 drag up
         * @param dragDownPercent y drag dragDownPercent
         */
        void onDrag(int dragDownX, int dragDownYOffset, float dragDownPercent);

        void onRelease(int dragDownX, int dragDownYOffset);
    }
}
