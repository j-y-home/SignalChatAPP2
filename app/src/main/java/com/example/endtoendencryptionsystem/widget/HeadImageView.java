package com.example.endtoendencryptionsystem.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;


import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.example.endtoendencryptionsystem.R;

public class HeadImageView extends AppCompatImageView {

    private String name = "";
    private String url = "";
    private int sizeResId = -1; // default size
    private Paint textPaint;
    private boolean showDefault = false;

    public HeadImageView(Context context) {
        super(context);
        init();
    }

    public HeadImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeadImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint = new Paint();
        textPaint.setTextSize(36);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setColor(Color.WHITE);

        this.setScaleType(ScaleType.CENTER_CROP);
    }

    public void setName(String name) {
        this.name = name;
        if (url == null || url.isEmpty()) {
            showDefault = true;
            invalidate();
        }
    }

    public void setUrl(String url) {
        this.url = url;
        if (url != null && !url.isEmpty()) {
            showDefault = false;
            Glide.with(getContext()).load(url).into(this);
        } else {
            showDefault = true;
            invalidate();
        }
    }

    public void setSize(int sizeResId) {
        this.sizeResId = sizeResId;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (showDefault) {
            drawDefaultAvatar(canvas);
        } else {
            super.onDraw(canvas);
        }
    }

    private void drawDefaultAvatar(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        float radius = Math.min(width, height) / 2f;
        // Generate background color based on name
        int bgColor = generateColorFromName(name);

        // Draw circle
        Paint bgPaint = new Paint();
        bgPaint.setColor(bgColor);
        canvas.drawCircle(width / 2f, height / 2f, radius, bgPaint);

        // Draw first letter
        if (name != null && !name.isEmpty()) {
            String letter = name.substring(0, 1).toUpperCase();
            float x = width / 2f;
            float y = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);
            canvas.drawText(letter, x, y, textPaint);
        }
    }

    private int generateColorFromName(String name) {
        int hash = 0;
        for (int i = 0; i < name.length(); i++) {
            hash += name.charAt(i);
        }
        int[] colors = {
                0xFF5daa31, // green
                0xFFc7515a, // red
                0xFFe03697, // purple
                0xFF85029b, // deep purple
                0xFFc9b455, // yellow
                0xFF326eb6  // blue
        };
        return colors[Math.abs(hash % colors.length)];
    }

//    @Override
//    protected void onMeasure(int widthSpec, int heightSpec) {
//        int size = getResources().getDimensionPixelSize(sizeResId != -1 ? sizeResId : R.dimen.default_avatar_size); // e.g., 96dp
//        setMeasuredDimension(size, size);
//    }
}
