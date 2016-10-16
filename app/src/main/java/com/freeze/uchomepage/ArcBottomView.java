package com.freeze.uchomepage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Administrator on 2016/10/11.
 */

public class ArcBottomView extends ViewGroup implements DragTracker.DragActionReceiver{

    private int rectStartHeight;
    private int rectHeight;
    private int arcHeight;
    private int backgroundColor;
    private Paint solidPaint;
    private Path path;

    protected int dragDownYOffset;
    private float arcYOffsetPercent = 0.33f;
    private int dragDownX;
    private Point dragPoint;
    private ValueAnimator releaseAnim;
    private boolean releaseAnimating;
    protected int dragDownYMaxOffset;

    public ArcBottomView(Context context) {
        super(context);
        init(context, null);
    }

    public ArcBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildAt(0) != null) {
            getChildAt(0).layout(l, t + (rectHeight - rectStartHeight), r, t + rectHeight);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(rectStartHeight + Math.max(0, dragDownYOffset), MeasureSpec.EXACTLY));
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (willNotDraw()) return false;
        return super.drawChild(canvas, child, drawingTime);
    }

    public void setBgColor(@ColorInt int color) {
        solidPaint.setColor(color);
    }

    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(false);
        rectStartHeight = Utils.dp2px(context, 224);
        dragDownYMaxOffset = rectStartHeight/2;
        rectHeight = rectStartHeight;
        arcHeight = 0;
        backgroundColor = Color.TRANSPARENT;
        solidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        solidPaint.setColor(backgroundColor);
        path = new Path();

        dragPoint = new Point(0, rectStartHeight);

        releaseAnim = ValueAnimator.ofInt().setDuration(Utils.RELEASE_DURATION);
        releaseAnim.setInterpolator(new OvershootInterpolator());
        releaseAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                innerDragProcess(dragDownX, value, value/dragDownYMaxOffset);
            }
        });

        releaseAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                releaseAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                releaseAnimating = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                releaseAnimating = true;
            }
        });

        onDrag(0, 0, 0);
    }

    public void onDrag(int dragDownX, int dragDownYOffset, float dragDownPercent) {
        if (releaseAnimating) return;
        if (dragDownYOffset < 0) {   //不响应向上的 drag 动作
            return;
        }
        this.dragDownX = dragDownX;
        this.dragDownYOffset = dragDownYOffset;

        rectHeight = Math.max(
                ((int) (dragDownYOffset * (1 - arcYOffsetPercent)) + rectStartHeight),
                rectStartHeight);
        arcHeight = (int) (dragDownYOffset * arcYOffsetPercent);
        dragPoint.set(dragDownX, rectHeight + 2 * arcHeight);
        requestLayout();
        invalidate(); //size 不变时 强制 invalidate
    }

    private void innerDragProcess(int dragDownX, int dragDownYOffset, float dragDownPercent) {
        if (dragDownYOffset < 0 && !releaseAnimating) {   //不响应向上的 drag 动作
            return;
        }
        this.dragDownX = dragDownX;
        this.dragDownYOffset = dragDownYOffset;

        rectHeight = Math.max(
                ((int) (dragDownYOffset * (1 - arcYOffsetPercent)) + rectStartHeight),
                rectStartHeight);
        arcHeight = (int) (dragDownYOffset * arcYOffsetPercent);
        dragPoint.set(dragDownX, rectHeight + 2 * arcHeight);
        requestLayout();
        invalidate(); //size 不变时 强制 invalidate
    }

    @Override
    public void onRelease(int dragDownX, int dragDownYOffset) {
        if (dragDownYOffset < 0 || dragDownYOffset >= dragDownYMaxOffset*DragTracker.DRAG_SECTION_RATE) return; //不响应向上的 drag 动作
        releaseAnim.setIntValues(dragDownYOffset, 0);
        releaseAnim.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        path.reset();
        path.lineTo(getMeasuredWidth(), 0);
        path.lineTo(getMeasuredWidth(), rectHeight);
        path.quadTo(dragPoint.x, dragPoint.y, 0, rectHeight);
        path.close();
        canvas.drawPath(path, solidPaint);
    }
}
