package com.hyfun.camera.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author z2wenfa
 * @description: 拍摄时长View
 * @date : 2021/12/21 10:23
 */
public class CaptureTimeView extends View {

    private Paint textPaint;

    /**
     * 拍摄持续时间
     */
    private long captureDuration = 0;

    public CaptureTimeView(Context context) {
        super(context);
        init();
    }

    public CaptureTimeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CaptureTimeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String duration = timeCalculate(captureDuration);

        drawText(canvas, duration, 0, (getBottom() - getTop() - textPaint.measureText(duration)) / 2, textPaint, 90);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        captureTimeHandler.removeCallbacks(null);
        captureTimeHandler.removeMessages(1);
    }

    private void init() {
        textPaint = new TextPaint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(dp2px(getContext(), 20));
        textPaint.setAntiAlias(true);
    }

    public String timeCalculate(long ttime) {
        long daysuuu, hoursuuu, minutesuuu, secondsuuu;
        String daysT = "", restT = "";
        daysuuu = (Math.round(ttime) / 86400);
        hoursuuu = (Math.round(ttime) / 3600) - (daysuuu * 24);
        minutesuuu = (Math.round(ttime) / 60) - (daysuuu * 1440) - (hoursuuu * 60);
        secondsuuu = Math.round(ttime) % 60;
        if (daysuuu == 1) daysT = String.format("%d day ", daysuuu);
        if (daysuuu > 1) daysT = String.format("%d days ", daysuuu);
        restT = String.format("%02d:%02d:%02d", hoursuuu, minutesuuu, secondsuuu);
        return daysT + restT;
    }

    void drawText(Canvas canvas, String text, float x, float y, Paint paint, float angle) {
        if (angle != 0) {
            canvas.rotate(angle, x, y);
        }
        canvas.drawText(text, x, y, paint);
        if (angle != 0) {
            canvas.rotate(-angle, x, y);
        }
    }

    /**
     * dp转换成px
     */
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void start() {
        captureDuration = 0;
        captureTimeHandler.sendEmptyMessageDelayed(1, 1000);
    }

    private Handler captureTimeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            captureDuration = captureDuration + 1;
            invalidate();
            captureTimeHandler.sendEmptyMessageDelayed(1, 1000);
        }
    };
}
