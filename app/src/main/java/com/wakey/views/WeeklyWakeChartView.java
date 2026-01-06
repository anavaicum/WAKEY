package com.wakey.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

    private int targetMinutes = -1;

    private Map<Integer, Integer> data = new HashMap<>();

    public WeeklyWakeChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        emptyPaint.setColor(0xFF2A324A);
        emptyPaint.setStyle(Paint.Style.FILL);
    }

    public void setTargetMinutes(int targetMinutes) {
        this.targetMinutes = targetMinutes;
        invalidate();
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

        // spațiu jos pentru zilele săptămânii
        int labelHeight = 40;
        int chartHeight = height - labelHeight;

        int barCount = 7;
        float spacing = width * 0.04f;
        float barWidth = (width - spacing * (barCount + 1)) / barCount;

        int maxMinutes = 8 * 60; // 08:00 referință (poți ajusta)

        // =====================
        // DESENARE BARE
        // =====================
        for (int i = 0; i < barCount; i++) {
            int day = i + 1; // Calendar.SUNDAY = 1
            int minutes = data.getOrDefault(day, 0);

            float ratio = Math.min(1f, (float) minutes / maxMinutes);
            float barHeight = chartHeight * ratio;

            float left = spacing + i * (barWidth + spacing);
            float top = chartHeight - barHeight;
            float right = left + barWidth;
            float bottom = chartHeight;

            RectF rect = new RectF(left, top, right, bottom);

            if (minutes == 0) {
                // zi fără date
                canvas.drawRoundRect(rect, 16f, 16f, emptyPaint);
            } else {
                boolean isEarly =
                        targetMinutes > 0 && minutes <= targetMinutes;

                int startColor;
                int endColor;

                if (isEarly) {
                    // verde = înainte / la target
                    startColor = 0xFF81C784;
                    endColor = 0xFF4CAF50;
                } else {
                    // roșu = după target
                    startColor = 0xFFFFAB91;
                    endColor = 0xFFE57373;
                }

                LinearGradient gradient = new LinearGradient(
                        0, top, 0, bottom,
                        startColor,
                        endColor,
                        Shader.TileMode.CLAMP
                );
                barPaint.setShader(gradient);
                canvas.drawRoundRect(rect, 16f, 16f, barPaint);
            }
        }

        // =====================
        // DESENARE ZILE
        // =====================
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.LTGRAY);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float daySpacing = width / 7f;
        float textY = height - 12;

        for (int i = 0; i < 7; i++) {
            float x = daySpacing * i + daySpacing / 2;
            canvas.drawText(days[i], x, textY, textPaint);
        }
    }

}
