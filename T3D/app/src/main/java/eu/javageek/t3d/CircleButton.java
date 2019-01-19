package eu.javageek.t3d;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import static java.lang.Math.abs;

public class CircleButton {

    float x, y, radius, radiusExt;
    String text;
    float textSize;

    public CircleButton(float x, float y, float radius, String text, float textSize) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.radiusExt = radius + 2;
        this.text = text;
        this.textSize = textSize;
    }

    public boolean isClick(double x, double y) {
        return (abs(this.x - x) < radiusExt && abs(this.y - y) < radiusExt);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);

        float[] widths = new float[1];
        paint.getTextWidths(text, widths);

        canvas.drawText(text, x - widths[0] / 2, y + textSize / 2, paint);
    }
}
