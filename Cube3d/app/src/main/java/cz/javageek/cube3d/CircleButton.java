package cz.javageek.cube3d;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

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

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        canvas.drawText(text, xText, yText, paint);
    }
}
