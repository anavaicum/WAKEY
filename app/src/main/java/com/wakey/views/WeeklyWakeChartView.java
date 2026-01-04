package com.wakey.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class WeeklyWakeChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // dayOfWeek -> minutes (Calendar.SUNDAY = 1 ... SATURDAY = 7)
    private Map<Integer, Integer> data = new HashMap<>();

    public WeeklyWakeChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        emptyPaint.setColor(0xFF2A324A);
        emptyPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(Map<Integer, Integer> data) {
        this.data = data;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int barCount = 7;
        float spacing = width * 0.04f;
        float barWidth = (width - spacing * (barCount + 1)) / barCount;

        int maxMinutes = 8 * 60; // 08:00 ca referință

        for (int i = 0; i < barCount; i++) {
            int day = i + 1; // 1=Sun
            int minutes = data.getOrDefault(day, 0);

            float ratio = Math.min(1f, (float) minutes / maxMinutes);
            float barHeight = height * ratio;

            float left = spacing + i * (barWidth + spacing);
            float top = height - barHeight;
            float right = left + barWidth;
            float bottom = height;

            RectF rect = new RectF(left, top, right, bottom);

            if (minutes == 0) {
                canvas.drawRoundRect(rect, 16f, 16f, emptyPaint);
            } else {
                LinearGradient gradient = new LinearGradient(
                        0, top, 0, bottom,
                        0xFFFFC77D,
                        0xFFFF8A65,
                        Shader.TileMode.CLAMP
                );
                barPaint.setShader(gradient);
                canvas.drawRoundRect(rect, 16f, 16f, barPaint);
            }
        }
    }
}
