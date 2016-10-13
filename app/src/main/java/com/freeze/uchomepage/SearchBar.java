package com.freeze.uchomepage;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2016/10/2.
 */

public class SearchBar extends ViewGroup implements ViewPager.OnPageChangeListener{

    private float zoomPercent = 1; // 1 min 0 max
    private View root;

    private int weatherHeight = Utils.dp2px(getContext(), 88);
    private int searchHeight = Utils.dp2px(getContext(), 64);
    private int menuHeight = Utils.dp2px(getContext(), 72);

    private int maxHeight = Utils.dp2px(getContext(), 224);
    private int searchContainerPaddingH = Utils.dp2px(getContext(), 16);
    private int minHeight = searchHeight;
    private ViewGroup menuGroup;
    private FrameLayout searchContainer;
    private LinearLayout searchArea;
    private int darkSearchBarColor;
    private int lightSearchBarColor;
    private ArgbEvaluator argbEvaluator;

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
            menuGroup.setAlpha((v - 0.5f)/0.5f);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (int) (minHeight + (1 - zoomPercent)*(maxHeight - minHeight)));
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        super.measureChild(child, parentWidthMeasureSpec, MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        root.layout(l, t - (int) (zoomPercent * weatherHeight), r, maxHeight - (int) (zoomPercent * weatherHeight));
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
}
