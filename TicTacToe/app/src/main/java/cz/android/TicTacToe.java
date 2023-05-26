package cz.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.util.Random;

public class TicTacToe extends Activity implements OnTouchListener {

    final char CHAR_X = 'x';
    final char CHAR_O = 'o';

    char[][] table = new char[3][3];

    Random random = new Random();

    DrawView drawView;

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
        float clickY = event.getY();

        float w = drawView.getWidth() / 3;
        float b = drawView.getHeight() / 2;

        System.out.println(drawView.getWidth());
        System.out.println(drawView.getHeight());

        // вычисляем ячейку щелчка
        int xT = (int) (clickX / w);
        int yT;
        if (clickY < b - w / 2) {
            yT = 0;
        } else if (clickY > b + w / 2) {
            yT = 2;
        } else {
            yT = 1;
        }

        // мы щелкнули по экрану
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            // если щелкнули внутри кнопки обнуления
            if (drawView.getBtnClear().isClick(clickX, clickY)) {
                initTable();
                drawView.invalidate();
                return true;
            }
            // если щелкнули внутри кнопки завершения
            if (drawView.getBtnExit().isClick(clickX, clickY)) {
                System.exit(0);
            }

            // если ячейка пустая и не победа ни моя, ни AI
            if (table[yT][xT] == 0 && !isWin(CHAR_O) && !isWin(CHAR_X)) {
                table[yT][xT] = CHAR_O;
                if (isWin(CHAR_O)) {
                   showAlert(R.string.you_won);
                } else if (isTableFull()) {
                    showAlert(R.string.sorry_draw);
                }

                if (!isWin(CHAR_O) && !isTableFull()) {
                    turnAI();
                    if (isWin(CHAR_X)) {
                        showAlert(R.string.ai_won);
                    }
                }
                drawView.invalidate();
            }
        }
        return true;
    }

    private void turnAI() {
        int xAI, yAI;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (table[x][y] == 0) {
                    table[x][y] = CHAR_O;
                    if (isWin(CHAR_O)) {
                        table[x][y] = CHAR_X;
                        return;
                    }
                    table[x][y] = 0;
                }
            }
        }
        do {
            xAI = random.nextInt(3);
            yAI = random.nextInt(3);
        } while (table[yAI][xAI] != 0);
        table[yAI][xAI] = CHAR_X;
    }

    private void initTable() {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                table[x][y] = 0;
            }
        }
    }

    private boolean isWin(char ch) {
        for (int i = 0; i < 3; i++) {
            if (table[i][0] == ch && table[i][1] == ch && table[i][2] == ch) return true;
            if (table[0][i] == ch && table[1][i] == ch && table[2][i] == ch) return true;
        }

        if (table[0][0] == ch && table[1][1] == ch && table[2][2] == ch) return true;
        if (table[2][0] == ch && table[1][1] == ch && table[0][2] == ch) return true;
        return false;
    }

    private boolean isTableFull() {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (table[x][y] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showAlert(int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TicTacToe.this);
        builder.setTitle(R.string.app_name)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
