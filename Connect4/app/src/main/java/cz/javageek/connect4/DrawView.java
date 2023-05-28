package cz.javageek.connect4;

import static cz.javageek.connect4.Connect4.COL;
import static cz.javageek.connect4.Connect4.ROW;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DrawView extends View {

    private char[][] table;
    private int xT;
    private int yT;



    public DrawView(Connect4 mainActivity, char[][] table) {
        super(mainActivity);
        this.table = table;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float dx = getWidth() / COL;
        float rd = (dx / 2) * 0.80f;
        float top = getHeight()- dx * ROW;

        Paint p = new Paint();
        p.setColor(Color.BLUE);
        p.setAntiAlias(true);

        canvas.drawRect(0, top, getWidth(), getHeight(), p);
        //p.setColor(Color.WHITE);
        //p.setStyle(Paint.Style.STROKE);

        for (int y = 0; y < ROW; y++) {
            for (int x = 0; x < COL; x++) {
                if (table[x][y] == 'x') {
                    p.setColor(Color.RED);
                } else if (table[x][y] == 'o') {
                    p.setColor(Color.YELLOW);
                } else {
                    p.setColor(Color.WHITE);
                }
                canvas.drawCircle(x * dx + dx / 2, y * dx + top + dx / 2, rd, p);
            }
        }
    }
}
