package cz.javageek.cube3d;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import static java.lang.Math.abs;

public class CircleButton {

    float x, y, radius;
    String text;
    float xText, yText;

    public CircleButton(float x, float y, float radius, String text, float xText, float yText) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.text = text;
        this.xText = xText;
        this.yText = yText;
    }

    public boolean isClick(double x, double y) {
        return (abs(this.x - x) < radius && abs(this.y - y) < radius);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        canvas.drawText(text, xText, yText, paint);
    }
}
