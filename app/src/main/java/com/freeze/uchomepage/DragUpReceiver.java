package com.freeze.uchomepage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2016/10/15.
 * 用来绘制主页向上拉时的效果，只能有一个子 view
 */

public class DragUpReceiver extends ViewGroup implements DragTracker.DragActionReceiver {

    public static int MAX_DRAG_UP_DIS = 224 + 100 - 64;
    public static int START_HEIGHT = 224 + 100;

    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

    private int startHeight = Utils.dp2px(getContext(), START_HEIGHT);
    private int maxDragUpDistance = Utils.dp2px(getContext(), MAX_DRAG_UP_DIS);  //上拉最大高度 = arcView + some_menu - topBar
    private int topBarHeight = Utils.dp2px(getContext(), 64);
    private int dragUpOffset = 0;
    private float dragUpPercent;

    private int bgColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
    private Paint bgPaint;
    private ArgbEvaluator argbEvaluator;
    private Drawable shadowBottom;
    private Drawable shadowTop;

    private int mScrimColor = DEFAULT_SCRIM_COLOR;
    private float mScrimOpacity;
    private boolean animating;
    private ValueAnimator releaseAnim;

    public DragUpReceiver(Context context) {
        super(context);
        init(context);
    }

    public DragUpReceiver(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(bgColor);
        bgPaint.setStyle(Paint.Style.FILL);

        argbEvaluator = new ArgbEvaluator();
        shadowBottom = ContextCompat.getDrawable(context, R.mipmap.shadow_bottom);
        shadowTop = ContextCompat.getDrawable(context, R.mipmap.shadow_top);

        releaseAnim = ValueAnimator.ofInt().setDuration(Utils.RELEASE_DURATION);
        releaseAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                onDrag(0, value, 0);
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (dragUpOffset == 0) {
            int height = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                childAt.measure(widthMeasureSpec, heightMeasureSpec);
                height += childAt.getMeasuredHeight();
            }
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        } else {
            int height = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                childAt.measure(widthMeasureSpec, heightMeasureSpec);
                height += childAt.getMeasuredHeight();
            }
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height - dragUpOffset, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (dragUpOffset != 0) {
            canvas.drawRect(0, 0, getMeasuredWidth(), getChildAt(0).getBottom(), bgPaint);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        canvas.save();
        if (dragUpOffset != 0) {
            canvas.scale(1 - dragUpPercent*0.1f, 1 - dragUpPercent*0.1f, getMeasuredWidth()/2, startHeight/2);
        }
        boolean b = super.drawChild(canvas, child, drawingTime);
        canvas.restore();
        if (dragUpOffset != 0) {
            drawShadow(canvas);
            drawScrim(canvas);
        }
        return b;
    }

    private void drawScrim(Canvas canvas) {
        final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
        final int alpha = (int) (baseAlpha * mScrimOpacity);
        final int color = alpha << 24 | (mScrimColor & 0xffffff);
        canvas.drawColor(color);
    }

    private void drawShadow(Canvas canvas) {
        shadowBottom.setAlpha((int) (mScrimOpacity * 255));
        shadowTop.setAlpha((int) (mScrimOpacity * 255));
        shadowBottom.setBounds(0, getMeasuredHeight() - shadowBottom.getIntrinsicHeight(), getMeasuredWidth(), getMeasuredHeight());
        shadowBottom.draw(canvas);
        shadowTop.setBounds(0, (int) (topBarHeight * dragUpPercent), getMeasuredWidth(), (int) (topBarHeight * dragUpPercent) + shadowTop.getIntrinsicHeight());
        shadowTop.draw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (dragUpOffset != 0) {
            t -= dragUpOffset/6;
        }
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            childAt.layout(l, t, childAt.getMeasuredWidth() + l, childAt.getMeasuredHeight() + t);
            t += childAt.getMeasuredHeight();
        }
    }

    @Override
    public void onDrag(int dragDownX, int dragDownYOffset, float dragDownPercent) {
        if (dragDownYOffset >= 0) {
            this.dragUpOffset = 0;
            this.dragUpPercent = 0f;
            requestLayout();
            return;
        }
        this.dragUpOffset = Math.min(maxDragUpDistance, Math.abs(dragDownYOffset));
        this.dragUpPercent = Math.min(1, dragUpOffset * 1f / maxDragUpDistance);

        mScrimOpacity = dragUpPercent;

        requestLayout();
    }

    @Override
    public void onRelease(int dragDownX, int dragDownYOffset) {
//        if (dragDownYOffset >= 0) return;
//
//        if (dragUpPercent > 0.5f) {
//            releaseAnim.setIntValues(-dragUpOffset, -maxDragUpDistance);
//        } else {
//            releaseAnim.setIntValues(-dragUpOffset, 0);
//        }
//        releaseAnim.start();
    }
}
