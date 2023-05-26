package cz.javageek.connect4;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DrawView extends View {

    private char[][] table;

    public DrawView(Connect4 mainActivity, char[][] table) {
        super(mainActivity);
        this.table = table;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float dx = getWidth()/7;
        float rd = (dx / 2) * 0.80f;
        float top = getHeight()- dx * 6;

        Paint p = new Paint();
        p.setColor(Color.BLUE);
        p.setAntiAlias(true);

        canvas.drawRect(0, top, getWidth(), getHeight(), p);
        p.setColor(Color.WHITE);
        //p.setStyle(Paint.Style.STROKE);

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 7; x++) {
                //canvas.drawCircle();
                canvas.drawCircle(x * dx + dx / 2, y * dx + top + dx / 2, rd, p);
            }
        }
    }
}
