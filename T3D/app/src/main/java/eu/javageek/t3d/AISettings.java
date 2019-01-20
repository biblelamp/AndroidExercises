package eu.javageek.t3d;

import android.graphics.Canvas;
import android.graphics.Paint;

public class AISettings {

    private CircleButton[] settings;

    public AISettings() {

    }

    public void draw(Canvas canvas, Paint paint) {
        for (CircleButton circle : settings) {
            circle.draw(canvas, paint);
        }
    }
}
