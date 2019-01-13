package eu.javageek.t3d;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import static java.lang.Math.abs;

public class CircleButton {

    float x, y, radius, radiusExt;
    String text;
    float xText, yText;

    public CircleButton(float x, float y, float radius, String text, float dx, float dy) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.radiusExt = radius + 2;
        this.text = text;
        this.xText = x + dx;
        this.yText = y + dy;
    }

    public boolean isClick(double x, double y) {
        return (abs(this.x - x) < radiusExt && abs(this.y - y) < radiusExt);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        canvas.drawText(text, xText, yText, paint);
    }
}
