package com.freeze.uchomepage.pullrefresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.text.TextPaint;
import android.view.animation.OvershootInterpolator;

import com.freeze.uchomepage.Utils;

/**
 * Created by wangqi on 2016/10/12.
 */

public class DoublePartRefreshDrawable extends RefreshDrawable {

    private int mHeight;
    private int mWidth;
    private final int refreshOffset;
    private final int overScrollOffset;

    private float mPercent;  // 0-1 refresh 1-1.8 toMainPage

    private float step1MarginTopPercent = 0.16f;
    private int step1TextMarginTop = Utils.dp2px(getContext(), 4);
    private int step1Radios = Utils.dp2px(getContext(), 16);
    private int step1TextSize = Utils.sp2px(getContext(), 14);
    private int step1RingWidth = Utils.dp2px(getContext(), 4);

    private int step3Radios = Utils.dp2px(getContext(), 24);
    private int step3HomeW = Utils.sp2px(getContext(), 16);
    private int step3HomeH = Utils.sp2px(getContext(), 12);

    private int step1RoundColor = Color.BLUE;
    private int step1RingColor = Color.WHITE;
    private int step1TextColor = Color.BLACK;
    private int step3RoundColor = Color.RED;

    private String step1String = "Loading...";
    private String step3String = "go home...";

    private float step1TextY;

    private final float step1TextLenPx;
    private final float step3TextLenPx;

    private final RectF step1RectF;
    private final RectF step1RingRectF;
    private final RectF step2UpOvalRectF;
    private final RectF step2DownOvalRectF;
    private final RectF step3UpOvalRectF;
    private final RectF step3DownOvalRectF;

    private final Path path;

    private final Paint step1Paint;
    private final TextPaint step1TextPaint;
    private final Paint step1RingPaint;
    private final Paint step3Paint;

    private final ValueAnimator step3Anim;
    private boolean step3Animating = false;

    private boolean isRunning;

    public DoublePartRefreshDrawable(Context context, PullRefreshLayout layout) {
        super(context, layout);

        path = new Path();
        step1RectF = new RectF();
        step1RingRectF = new RectF();

        step1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        step1Paint.setStyle(Paint.Style.FILL);

        step1TextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        step1TextPaint.setTextSize(step1TextSize);
//        Paint.FontMetrics fontMetrics = step1TextPaint.getFontMetrics();
        step1TextPaint.setColor(step1TextColor);
        step1TextLenPx = step1TextPaint.measureText(step1String);
        step3TextLenPx = step1TextPaint.measureText(step3String);

        step1RingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        step1RingPaint.setStyle(Paint.Style.STROKE);
//        step1RingPaint.setDither(true);
        step1RingPaint.setStrokeJoin(Paint.Join.ROUND); //相交
        step1RingPaint.setStrokeCap(Paint.Cap.ROUND);  //结尾
        step1RingPaint.setStrokeWidth(step1RingWidth);

        step2UpOvalRectF = new RectF();
        step2DownOvalRectF = new RectF();

        step3Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        step3Paint.setStyle(Paint.Style.FILL);
        step3Paint.setColor(step3RoundColor);

        refreshOffset = getRefreshLayout().getFinalOffset();
        overScrollOffset = refreshOffset * 2;

        step3UpOvalRectF = new RectF();
        step3DownOvalRectF = new RectF();

        step3Anim = ValueAnimator.ofInt(100, 0).setDuration(300);
        step3Anim.setInterpolator(new OvershootInterpolator());
        step3Anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float percent = ((int) animation.getAnimatedValue()) / 100f;
                float centerX = mWidth / 2;
                float centerY = overScrollOffset - refreshOffset;
                float ovalWidthRadios = step3Radios * (1 - percent * 0.5f);
                float upOvalHeightRadios = step3Radios * (1 + percent * 0.5f);
                float downOvalHeightRadios = step3Radios;
                step3UpOvalRectF.set(centerX - ovalWidthRadios, centerY - upOvalHeightRadios, centerX + ovalWidthRadios, centerY + upOvalHeightRadios);
                step3DownOvalRectF.set(centerX - ovalWidthRadios, centerY - downOvalHeightRadios, centerX + ovalWidthRadios, centerY + downOvalHeightRadios);
                invalidateSelf();
            }
        });
        step3Anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                step3Animating = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                step3Animating = true;
            }
        });
    }

    @Override
    public void setPercent(float percent) {

    }

    @Override
    public void setColorSchemeColors(int[] colorSchemeColors) {

    }

    @Override
    public void offsetTopAndBottom(int offset) {
        float prePercent = mHeight * 1f / refreshOffset;
        mHeight += offset;
        mPercent = mHeight * 1f / refreshOffset;

        if (mPercent <= 1) {
            // draw 文字的 y
            step1TextY = mHeight * step1MarginTopPercent + step1Radios * 2 + step1TextMarginTop + step1TextSize;
        } else {
            // mPercent > 1 的时候 下椭圆高度变长，文字随之一起向下
            step1TextY = mHeight * step1MarginTopPercent + step1Radios * 2 + step1TextMarginTop + step1TextSize + (mPercent - 1) * 2 * step1Radios;
            // mPercent > 1 的时候 文字颜色变淡
            if (mPercent < 1.8f) {
                step1TextPaint.setColor(ColorUtils.setAlphaComponent(step1TextColor, (int) (255*(1.8f - mPercent)/0.8f)));
            }
        }

        //第一步圆的位置
        step1RectF.set(mWidth / 2 - step1Radios,
                mHeight * step1MarginTopPercent,
                mWidth / 2 + step1Radios,
                mHeight * step1MarginTopPercent + step1Radios * 2);

        if (mPercent <= 1) {
            if (mPercent > 0.8f) {  // 出现圆环
                // 设置圆环位置
                step1RingRectF.set(step1RectF.left + step1RingWidth * 2,
                        step1RectF.top + step1RingWidth * 2,
                        step1RectF.right - step1RingWidth * 2,
                        step1RectF.bottom - step1RingWidth * 2);
                // 圆环颜色变深
                step1RingPaint.setColor(ColorUtils.setAlphaComponent(step1RingColor, (int) (255 * (mPercent - 0.8f) / 0.2f)));
            }
            //第一步圆颜色变深
            step1Paint.setColor(ColorUtils.setAlphaComponent(step1RoundColor, (int) (255 * mPercent)));
        }

        if (mPercent >= 1 && mPercent < 1.8) {

            // 由画圆变成 画 上下两个不同长度的椭圆
            // 上椭圆高度不变，宽度变小
            step2UpOvalRectF.set(mWidth / 2 - step1Radios + (mPercent - 1) * 0.2f * step1Radios,
                    refreshOffset * step1MarginTopPercent,
                    mWidth / 2 + step1Radios - (mPercent - 1) * 0.2f * step1Radios,
                    refreshOffset * step1MarginTopPercent + step1Radios * 2);
            //下椭圆高度变长，宽度变小
            step2DownOvalRectF.set(mWidth / 2 - step1Radios + (mPercent - 1) * 0.2f * step1Radios,
                    refreshOffset * step1MarginTopPercent - (mPercent - 1) * 2 * step1Radios,
                    mWidth / 2 + step1Radios - (mPercent - 1) * 0.2f * step1Radios,
                    refreshOffset * step1MarginTopPercent + step1Radios * 2 + (mPercent - 1) * 2 * step1Radios);

            //缩小 270° 的环
            step1RingRectF.set(step1RectF.left + step1RingWidth * (1 + mPercent),
                    step1RectF.top + step1RingWidth * (1 + mPercent),
                    step1RectF.right - step1RingWidth * (1 + mPercent),
                    step1RectF.bottom - step1RingWidth * (1 + mPercent));
            //变淡 270° 的环
            step1RingPaint.setColor(ColorUtils.setAlphaComponent(step1RingColor, (int) (255*(1.8f - mPercent)/0.8f)));
        }

        if (mPercent >= 1.8f) {
            //变深的 home 图案
            step1RingPaint.setColor(ColorUtils.setAlphaComponent(step1RingColor, (int) (255 * Math.max(((mPercent - 1.8f) / 0.2f), 0.5f))));
            //变深 go home 文字
            step1TextPaint.setColor(ColorUtils.setAlphaComponent(step1TextColor, (int) (255*(mPercent - 1.8f)/0.2f)));
        }

        if (mPercent > 1.8f && prePercent <= 1.8f && !step3Animating) {
            // 开始绘制 返回主页的圆，一个自动执行的动画
            step3Anim.start();
        }

        if (mPercent <= 1.8f && prePercent > 1.8f && !step3Animating) {
            // 反转绘制 返回主页的圆，一个自动执行的动画
            step3Anim.reverse();
        }

        if (!step3Animating) {
            invalidateSelf();
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mWidth = bounds.width();
    }

    private int refreshingDegree;
    private int refreshingArcSweep = 10;
    private int refreshingArcStart;

    @SuppressLint("HandlerLeak")
    private Handler mHandle = new Handler() {

        int countDown1 = 0;
        int countDown2 = 0;

        @Override
        public void handleMessage(Message msg) {
            refreshingDegree += 2;

            if (refreshingArcSweep == 10) {
                countDown1 = 30;
            }

            if (countDown1 != 0) {
                refreshingArcSweep += 10;
                countDown1--;
            }

            if (refreshingArcSweep == 310) {
                countDown2 = 30;
            }

            if (countDown2 != 0) {
                refreshingArcStart += 10;
                refreshingArcSweep -= 10;
                countDown2--;
            }
            invalidateSelf();
            sendEmptyMessageDelayed(0, 20);
        }
    };

    @Override
    public void start() {
        mHandle.sendEmptyMessageDelayed(0, 20);
        isRunning = true;
    }

    @Override
    public void stop() {
        refreshingDegree = 0;
        refreshingArcSweep = 10;
        refreshingArcStart = 0;
        isRunning = false;
        mHandle.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        if (step3Animating) {
            //draw home round anim
            path.reset();
            path.arcTo(step3UpOvalRectF, -180, 180);
            path.arcTo(step3DownOvalRectF, 0, 180);
            path.close();
            canvas.drawPath(path, step3Paint);

            return;
        }

        if (mPercent < 0.8f) {
            path.reset();
            path.moveTo(mWidth / 2, mHeight * step1MarginTopPercent + step1Radios);
            path.lineTo(mWidth / 2, mHeight * step1MarginTopPercent - step1Radios);
            float roundPercent = Math.min(mPercent / 0.8f, 1f);
            path.arcTo(step1RectF, -90, roundPercent * 360);
            path.close();
            canvas.drawPath(path, step1Paint);
        } else if (mPercent >= 0.8f && mPercent < 1f) {
            canvas.drawCircle(mWidth / 2, mHeight * step1MarginTopPercent + step1Radios, step1Radios, step1Paint);
        } else if (mPercent >= 1f && mPercent < 1.8f) {
            path.reset();
            path.arcTo(step2UpOvalRectF, -180, 180);
            path.arcTo(step2DownOvalRectF, 0, 180);
            path.close();
            canvas.drawPath(path, step1Paint);
        } else {
            //draw home round
            path.reset();
            path.arcTo(step3UpOvalRectF, -180, 180);
            path.arcTo(step3DownOvalRectF, 0, 180);
            path.close();
            canvas.drawPath(path, step3Paint);

            //draw home
            float centerX = mWidth / 2;
            float centerY = overScrollOffset - refreshOffset;
            path.reset();
            path.addRect(centerX - step3HomeW /2, centerY - step3HomeH /2, centerX + step3HomeW /2, centerY + step3HomeH /2, Path.Direction.CW);
            path.moveTo(centerX - step3HomeW /2, centerY - step3HomeH /2);
            path.lineTo(centerX, centerY - step3HomeH *3/4);
            path.lineTo(centerX + step3HomeW /2, centerY - step3HomeH /2);
            canvas.drawPath(path, step1RingPaint);

            //draw text

            canvas.save();
            canvas.translate(mWidth / 2 - step3TextLenPx / 2, centerY + step3Radios + step1TextMarginTop + step1TextSize);
            canvas.drawText(step3String, 0, 0, step1TextPaint);
            canvas.restore();

        }

        if (mPercent < 1.8f) {
            //draw step1 text
            canvas.save();
            canvas.translate(mWidth / 2 - step1TextLenPx / 2, step1TextY);
            canvas.drawText(step1String, 0, 0, step1TextPaint);
            canvas.restore();
        }

        // draw ring
        if (mPercent > 0.8f && mPercent < 1.8f) {
            canvas.save();
            if (isRunning()) {
                canvas.rotate(refreshingDegree, mWidth / 2, mHeight * step1MarginTopPercent + step1Radios);
                canvas.drawArc(step1RingRectF, refreshingArcStart, refreshingArcSweep, false, step1RingPaint);
            } else {
                canvas.drawArc(step1RingRectF, mPercent >= 1? (180 * (1 - 0.8f) / 0.2f) : (180 * (mPercent - 0.8f) / 0.2f), 270, false, step1RingPaint);
            }
            canvas.restore();
        }
    }
}
