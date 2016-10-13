package com.freeze.uchomepage;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2016/10/11.
 */

public class ArcBottomView extends View implements DragTrackLinearLayout.DragActionReceiver{

    private int rectStartHeight;
    private int rectHeight;
    private int arcHeight;
    private int backgroundColor;
    private Paint solidPaint;
    private Path path;

    private int dragDownYOffset;
    private float arcYOffsetPercent = 0.33f;
    private int dragDownYMaxOffset;
    private int dragDownX;
    private int mWidth;
    private Point dragPoint;

    public ArcBottomView(Context context) {
        super(context);
        init(context, null);
    }

    public ArcBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(rectStartHeight + Math.min(dragDownYOffset, dragDownYMaxOffset), MeasureSpec.EXACTLY));
    }

    private void init(Context context, AttributeSet attrs) {
        rectStartHeight = Utils.dp2px(context, 224);
        rectHeight = rectStartHeight;
        arcHeight = 0;
        dragDownYMaxOffset = rectStartHeight / 2;
        backgroundColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        solidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        solidPaint.setColor(backgroundColor);
        path = new Path();

        dragPoint = new Point(0, rectStartHeight);

        new InterC

        postDelayed(new Runnable() {
            @Override
            public void run() {
                onDrag(getWidth()/3, 50);
            }
        }, 3000);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
    }

    public void onDrag(int dragDownX, int dragDownYOffset) {
        this.dragDownX = dragDownX;
        this.dragDownYOffset = dragDownYOffset;
        rectHeight = (int) (Math.min(dragDownYOffset, dragDownYMaxOffset) * (1 - arcYOffsetPercent) + rectStartHeight);
        arcHeight = (int) (Math.min(dragDownYOffset, dragDownYMaxOffset) * arcYOffsetPercent);
        dragPoint.set(dragDownX, rectHeight + 2 * arcHeight);
        requestLayout();
    }

    @Override
    public void onRelease(int dragDownX, int dragDownYOffset) {
        ValueAnimator animator = ValueAnimator.ofInt(dragDownYOffset, 0).setDuration(300);
        animator.setInterpolator(new );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        path.lineTo(getMeasuredWidth(), 0);
        path.lineTo(getMeasuredWidth(), rectHeight);
        path.quadTo(dragPoint.x, dragPoint.y, 0, rectHeight);
        path.close();
        canvas.drawPath(path, solidPaint);
    }
}
