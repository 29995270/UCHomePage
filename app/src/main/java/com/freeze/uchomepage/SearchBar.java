package com.freeze.uchomepage;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2016/10/2.
 */

public class SearchBar extends ArcBottomView implements ViewPager.OnPageChangeListener {

    private float zoomPercent = 1; // 1 min 0 max
    private View root;

    private int weatherHeight = Utils.dp2px(getContext(), 88);
    private int searchHeight = Utils.dp2px(getContext(), 64);
    private int menuHeight = Utils.dp2px(getContext(), 72);

    private int maxHeight = Utils.dp2px(getContext(), 224);
    private int searchContainerPaddingH = Utils.dp2px(getContext(), 16);

    private int refreshArrowWidth = Utils.dp2px(getContext(), 4);
    private int refreshTextSize = Utils.sp2px(getContext(), 14);
    private int refreshTextMarginTop = Utils.dp2px(getContext(), 8);
    private String refreshText = "刷新并进入头条";
    private float refreshTextLenPx;

    private int minHeight = searchHeight;
    private ViewGroup menuGroup;
    private FrameLayout searchContainer;
    private LinearLayout searchArea;
    private int darkSearchBarColor;
    private int lightSearchBarColor;
    private ArgbEvaluator argbEvaluator;
    private float dragDownPercent;
    private Paint refreshPaint;
    private TextPaint refreshTextPaint;

    public SearchBar(Context context) {
        super(context);
        init(context);
    }

    public SearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        ViewGroup thisView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.widget_search_bar, this, true);
        menuGroup = (ViewGroup) thisView.findViewById(R.id.menu);
        searchContainer = (FrameLayout) findViewById(R.id.search_container);
        searchArea = (LinearLayout) findViewById(R.id.search_area);
        this.root = thisView.getChildAt(0);

        setMenuAlpha(1 - zoomPercent);

        darkSearchBarColor = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        lightSearchBarColor = ContextCompat.getColor(context, R.color.colorPrimary);
        argbEvaluator = new ArgbEvaluator();
        setSearchBarColor(zoomPercent);
        setSearchContainerPaddingH(1f - zoomPercent);

        setBgColor(darkSearchBarColor);

        refreshPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        refreshPaint.setStrokeJoin(Paint.Join.ROUND);
        refreshPaint.setStrokeCap(Paint.Cap.ROUND);
        refreshPaint.setStrokeWidth(refreshArrowWidth);

        refreshTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        refreshTextPaint.setTextSize(refreshTextSize);
        refreshTextPaint.setColor(refreshColor);
        refreshTextLenPx = refreshTextPaint.measureText(refreshText);
    }

    private void setSearchContainerPaddingH(float v) {
        searchContainer.setPadding(((int) (searchContainerPaddingH * v)), 0, (int) (searchContainerPaddingH * v), 0);
    }

    private void setSearchBarColor(float v) {
        searchArea.setBackgroundColor((Integer) argbEvaluator.evaluate(v, lightSearchBarColor, darkSearchBarColor));
    }

    private void setMenuAlpha(float v) {
        if (v < 0.5f) {
            menuGroup.setVisibility(INVISIBLE);
        } else {
            menuGroup.setVisibility(VISIBLE);
            menuGroup.setAlpha((v - 0.5f) / 0.5f);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (dragDownYOffset == 0) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                    (int) (minHeight + (1 - zoomPercent) * (maxHeight - minHeight)));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        super.measureChild(child, parentWidthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (dragDownYOffset == 0) {
            root.layout(l, t - (int) (zoomPercent * weatherHeight), r, maxHeight - (int) (zoomPercent * weatherHeight));
        } else {
            super.onLayout(changed, l, t, r, b);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child == root) {
            float scaleRate = 1 - Math.max(0f, dragDownPercent) * 0.1f;
            canvas.scale(scaleRate, scaleRate, child.getLeft() + child.getMeasuredWidth() / 2, child.getTop() + child.getMeasuredHeight() / 2);
        }

        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (dragDownYOffset == 0) {
            canvas.drawColor(darkSearchBarColor);
        } else {
            super.onDraw(canvas);
            drawRefreshBall(canvas);
        }
    }

    private int refreshBallMaxRadios = Utils.dp2px(getContext(), 16);
    private int refreshColor = Color.WHITE;

    private void drawRefreshBall(Canvas canvas) {

        canvas.save();
        int centerX = getMeasuredWidth() / 2;
        int centerY = maxHeight * 2 / 3;
        float percent = Math.min(dragDownPercent/DragTracker.DRAG_SECTION_RATE, 1);
        if (percent < 0) percent = 0;

        if (dragDownPercent > DragTracker.DRAG_SECTION_RATE) {
            canvas.translate(0, dragDownYOffset - dragDownYMaxOffset*DragTracker.DRAG_SECTION_RATE);
        }

        canvas.save();
        canvas.rotate(180 * percent, getMeasuredWidth() / 2, maxHeight * 2 / 3);
        canvas.scale(percent, percent, centerX, centerY);
        //draw circle
        int alpha = (int) (255 * percent);
        refreshPaint.setColor(ColorUtils.setAlphaComponent(refreshColor, alpha));
        refreshTextPaint.setColor(ColorUtils.setAlphaComponent(refreshColor, alpha));
        refreshPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(getMeasuredWidth() / 2, maxHeight * 2 / 3, refreshBallMaxRadios, refreshPaint);

        //draw arrow
        refreshPaint.setColor(ColorUtils.setAlphaComponent(darkSearchBarColor, alpha));
        refreshPaint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(centerX - refreshBallMaxRadios/2, centerY - refreshBallMaxRadios/4, centerX, centerY + refreshBallMaxRadios/4, refreshPaint);
        canvas.drawLine(centerX + refreshBallMaxRadios/2, centerY - refreshBallMaxRadios/4, centerX, centerY + refreshBallMaxRadios/4, refreshPaint);
        canvas.restore();

        //draw text
        canvas.drawText(refreshText, centerX - refreshTextLenPx/2, centerY + refreshBallMaxRadios + refreshTextMarginTop + refreshTextSize, refreshTextPaint);

        canvas.restore();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionOffset == 0.0f) { // to skip on page select
            return;
        }
        if (positionOffset < 0.05f) {
            zoomPercent = 0;
            setMenuAlpha(1);
            setSearchBarColor(0);
            setSearchContainerPaddingH(1);
        } else {
            zoomPercent = positionOffset;
            setMenuAlpha(1 - zoomPercent);
            setSearchBarColor(zoomPercent);
            setSearchContainerPaddingH(1 - zoomPercent);
        }
        requestLayout();
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void expendOrCollapse(boolean expendOrCollapse) {
        onPageScrolled(0, expendOrCollapse? (float) 0.01 : 1, 0);
    }

    @Override
    public void onDrag(int dragDownX, int dragDownYOffset, float dragDownPercent) {
        super.onDrag(dragDownX, dragDownYOffset, dragDownPercent);
        this.dragDownPercent = dragDownPercent;
        if (root != null) {
            root.setAlpha(1 - Math.min(this.dragDownPercent /DragTracker.DRAG_SECTION_RATE, 1));
        }
    }

    @Override
    public void onRelease(int dragDownX, int dragDownYOffset) {
        super.onRelease(dragDownX, dragDownYOffset);
    }
}
