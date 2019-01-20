package eu.javageek.t3d;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import static java.lang.Math.abs;

public class CircleButton {

    private float x, y, radius, radiusExt;
    private String text;
    private float textSize;
    private int btnColor;

    public CircleButton(float x, float y, float radius, String text, float textSize) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.radiusExt = radius + 2;
        this.text = text;
        this.textSize = textSize;
        this.btnColor = Color.WHITE;
    }

    public void setBtnColor(int btnColor) {
        this.btnColor = btnColor;
    }

    public boolean isClick(double x, double y) {
        return (abs(this.x - x) < radiusExt && abs(this.y - y) < radiusExt);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(btnColor);
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);

        float[] widths = new float[1];
        paint.getTextWidths(text, widths);

        canvas.drawText(text, x - widths[0] / 2, y + textSize / 2, paint);
    }
}
