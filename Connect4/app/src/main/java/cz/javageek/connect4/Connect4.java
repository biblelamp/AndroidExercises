package cz.javageek.connect4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.metrics.Event;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class Connect4 extends Activity implements OnTouchListener {

    static final int COL = 7;
    static final int ROW = 6;
    static final int WIN = 4;

    private char[][] table = new char[COL][ROW];

    private DrawView drawView;

    private boolean myTurn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawView = new DrawView(this, table);
        drawView.setOnTouchListener(this);
        setContentView(drawView);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float clickX = event.getX();
        float dx = drawView.getWidth() / COL;

        // колонка куда падает фишка
        int col = (int) (clickX / dx);

        // мы щелкнули по экрану
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int row = ROW - 1; row >= 0; row--) {
                if (table[col][row] == 0) {
                    table[col][row] = myTurn ? 'x' : 'o';
                    myTurn = ! myTurn;
                    break;
                }
            }
            drawView.invalidate();
        }

        // проверка победы
        isWin('x');

        return true;
    }

    private boolean isWin(char ch) {
        for (int y = 0; y < ROW; y++) {
            for (int x = 0; x < COL; x++) {

            }
        }
        return false;
    }

    private int countChar(int x, int y, char ch, int dx, int dy) {
        return 0;
    }
}
