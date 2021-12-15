package com.hyfun.camera.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.hyfun.camera.R;

/**
 * @Description 边框
 * @Author wenfa
 * @Date 2021/12/15 20:44
 */
public class BoxView extends View {


    @DrawableRes
    public static int iconRes = R.drawable.person; // 图片资源

    Paint paint = new Paint();

    private int lineWidth;// 线宽
    private int lineLength;// 线长
    private int margin;// 边框间距

    private int bitmapMargin;
    private Bitmap bitmap;

    Paint textPaint;
    private int textMargin;

    public BoxView(Context context) {
        super(context);
        initPaint();
    }

    public BoxView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public BoxView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制四个边角
        drawBox(canvas);

        // 绘制头像
        drawBitmap(canvas);
    }


    // 绘制边角
    private void drawBox(Canvas canvas) {
        canvas.drawLine(margin, margin, margin + lineLength, margin, paint);
        canvas.drawLine(margin, margin, margin, margin + lineLength, paint);

        canvas.drawLine(getRight() - margin, margin, getRight() - margin - lineLength, margin, paint);
        canvas.drawLine(getRight() - margin, margin, getRight() - margin, margin + lineLength, paint);

        canvas.drawLine(margin, getBottom() - margin, margin, getBottom() - margin - lineLength, paint);
        canvas.drawLine(margin, getBottom() - margin, margin + lineLength, getBottom() - margin, paint);

        canvas.drawLine(getRight() - margin, getBottom() - margin, getRight() - margin, getBottom() - margin - lineLength, paint);
        canvas.drawLine(getRight() - margin, getBottom() - margin, getRight() - margin - lineLength, getBottom() - margin, paint);
    }

    /**
     * 绘制图像
     *
     * @param canvas
     */
    private void drawBitmap(Canvas canvas) {
        Matrix matrix2 = getBitmapMatrix(canvas, -1, "B岗");
        Matrix matrix1 = getBitmapMatrix(canvas, 0, "客户");
        Matrix matrix = getBitmapMatrix(canvas, 1, "A岗");
        canvas.drawBitmap(bitmap, matrix, paint);
        canvas.drawBitmap(bitmap, matrix1, paint);
        canvas.drawBitmap(bitmap, matrix2, paint);
    }

    /**
     * 获取Bitmap展示Matrix
     *
     * @param index 中间为0 上方为正值 下方为负值
     * @return
     */
    private Matrix getBitmapMatrix(Canvas canvas, int index, String info) {
        Matrix matrix = new Matrix();

        int bw = bitmap.getWidth();
        int bh = bitmap.getHeight();

        float rate = getWidth() / 2 / bw;

        // 大于最大高度重新计算比率
        if (rate * bh * 3 > getHeight()) {
            rate = rate - 1;
        }

        float bwScaledWidth = bw * rate;
        float bwScaleHeight = bh * rate;

        int marginTop = (int) ((getBottom() - getLeft() - bwScaleHeight) / 2);

        matrix.postRotate(90);
        matrix.postScale(rate, rate);

        float dx = (getRight() - getLeft() + bwScaledWidth) / 2;
        float dy = marginTop - (bwScaleHeight * index);

        matrix.postTranslate(dx, dy);

        // 绘制文案
        drawText(canvas, info, dx - bwScaledWidth - textMargin, dy + bwScaleHeight / 2 - textPaint.measureText(info) / 2, textPaint, 90);

        return matrix;
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


    private void initPaint() {

        lineWidth = dp2px(getContext(), 3);
        lineLength = dp2px(getContext(), 16);
        margin = dp2px(getContext(), 8);

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        paint.setColor(Color.parseColor("#4491C9"));

        bitmap = BitmapFactory.decodeResource(getResources(), iconRes);

        bitmapMargin = dp2px(getContext(), 100);

        textPaint = new TextPaint();
        textPaint.setColor(Color.parseColor("#4491C9"));
        textPaint.setTextSize(dp2px(getContext(), 20));
        textPaint.setAntiAlias(true);
        textMargin = dp2px(getContext(), 40);
    }

    /**
     * dp转换成px
     */
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
