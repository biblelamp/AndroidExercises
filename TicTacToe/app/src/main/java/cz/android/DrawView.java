package cz.android;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

class DrawView extends View {

    private char[][] table;

    // кнопки обнуления и выхода из игры
    private float btnClearX, btnClearY, btnExitX, btnExitY, btnR;

    private Button btnClear, btnExit;

    public DrawView(TicTacToe mainActivity, char[][] table) {
        super(mainActivity);
        this.table = table;
    }

    public Button getBtnClear() {
        return btnClear;
    }

    public Button getBtnExit() {
        return btnExit;
    }

    private void drawExit(Canvas canvas) {
        // рассчитываем размеры и координаты окружности

        // назначаем цвет и толщину и рисуем
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float w = getWidth() / 3;
        float b = getHeight() / 2;

        btnExitX = w*2 + w/2;
        btnExitY = b - b/2 - b/3;
        btnR = w/3;

        btnExit = new Button(btnExitX, btnExitY, btnR);

        btnClearX = btnExitX - btnR * 2.5f;
        btnClearY = b - b/2 - b/3;

        btnClear = new Button(btnClearX, btnClearY, btnR);

        float lineWidth = 12 * getWidth() / 480;
        float singWidth = 25 * getWidth() / 480;

        // расчерчиваем игровое поле
        Paint p = new Paint();
        p.setColor(Color.LTGRAY);
        p.setStrokeWidth(lineWidth);
        p.setStyle(Paint.Style.STROKE);
        p.setAntiAlias(true);

        canvas.drawLine(0, b - w / 2, getWidth(), b - w / 2, p);
        canvas.drawLine(0, b + w / 2, getWidth(), b + w / 2, p);
        canvas.drawLine(w, getHeight() / 2 - getWidth() / 2, w, getHeight() / 2 + getWidth() / 2, p);
        canvas.drawLine(w + w, getHeight() / 2 - getWidth() / 2, w + w, getHeight() / 2 + getWidth() / 2, p);

        drawExit(canvas);

        // толщина линий для крестиков-ноликов
        p.setStrokeWidth(singWidth);

        // рисуем содержимое таблицы
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (table[row][col] == 'o') {
                    p.setColor(Color.BLUE);
                    float x = col * w + w / 2;
                    float y = (float) ((b - w * 1.5) + row * w) + w / 2;
                    canvas.drawCircle(x, y, w / 2, p);
                }
                if (table[row][col] == 'x') {
                    p.setColor(Color.RED);
                    float x1 = col * w + w / 2;
                    float y1 = (float) ((b - w * 1.5) + row * w) + w / 2;
                    canvas.drawLine(x1 - w/2, y1 + w/2  , x1 + w/2, y1-w/2, p);
                    canvas.drawLine(x1 + w/2, y1 + w/2  , x1 - w/2, y1-w/2, p);
                }
            }
        }
        p.setColor(Color.BLUE);
        p.setStrokeWidth(singWidth);
        canvas.drawCircle(btnClearX, btnClearY, btnR, p);
        canvas.drawCircle(btnExitX,btnExitY,btnR,p);
        p.setColor(Color.RED);
        canvas.drawArc(btnClearX - btnR /2 + btnR /4, btnClearY - btnR /2 + btnR /4,
                btnClearX + btnR /4, btnClearY + btnR /4,
                45F, 270F, false, p);
        canvas.drawLine(btnExitX - w/3 + w/5 - w/10,btnExitY + w/3 -w/7 ,btnExitX + w/3 - w/7 + w/17,btnExitY - w/3 + w/7,p);
        canvas.drawLine(btnExitX - w/3 + w/5 - w/10,btnExitY - w/3 +w/7,btnExitX + w/3 - w/7 + w/17,btnExitY + w/3 - w/7,p);
    }
}
