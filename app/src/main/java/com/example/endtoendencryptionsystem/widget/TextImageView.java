package com.example.endtoendencryptionsystem.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import java.util.Random;

public class TextImageView extends AppCompatImageView {
    private String text = "";
    private int backgroundColor;
    private Paint textPaint;
    private Rect textBounds = new Rect();

    public TextImageView(Context context) {
        super(context);
        init();
    }

    public TextImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 初始化画笔
        textPaint = new Paint();
        textPaint.setTextSize(65); // 设置字体大小
        textPaint.setColor(Color.WHITE); // 设置文字颜色
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 随机生成背景颜色
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        backgroundColor = Color.rgb(red, green, blue);
    }

    public void setText(String text) {
        this.text = text;
        invalidate(); // 刷新视图
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        float radius = Math.min(width, height) / 2f;

        // 创建圆形路径
        Path path = new Path();
        path.addCircle(width / 2f, height / 2f, radius, Path.Direction.CW);
        canvas.clipPath(path);

        // 清除画布
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // 绘制圆形背景
        canvas.drawCircle(width / 2f, height / 2f, radius, getBackgroundPaint());

        // 绘制文字
        if (text != null && !text.isEmpty()) {
            textPaint.getTextBounds(text, 0, text.length(), textBounds);
            float x = width / 2f;
            float y = height / 2f + textBounds.height() / 2f;
            canvas.drawText(text, x, y, textPaint);
        }
    }
    private Paint getBackgroundPaint() {
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setAntiAlias(true);
        return backgroundPaint;
    }
}
