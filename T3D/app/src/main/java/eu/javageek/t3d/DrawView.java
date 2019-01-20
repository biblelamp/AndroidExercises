package eu.javageek.t3d;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.Display;
import android.view.View;

import java.util.Random;

import static java.lang.Math.*;

public class DrawView extends View {

    private double[][] nodes = new double[27][4];

    private int[][] edges = {
            {0, 2}, {3, 5}, {6, 8}, {0, 6}, {1, 7}, {2, 8},
            {9, 11}, {12, 14}, {15, 17}, {9, 15}, {10, 16}, {11, 17},
            {18, 20}, {21, 23}, {24, 26}, {18, 24}, {19, 25}, {20, 26},
            {0, 18}, {1, 19}, {2, 20}, {3, 21}, {4, 22}, {5, 23}, {6, 24}, {7, 25}, {8, 26}
    };

    private Paint paint;
    private CircleButton btnClear, btnAbout, btnExit;
    private float heightTitle;
    private float titleTextSize;
    private float radius;
    private float sensitivity;

    private Random random;

    public DrawView(MainActivity mainActivity) {
        super(mainActivity);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        Display display = mainActivity.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        initNodes();
        scale(min(width, height) / 4);

        random = new Random();

        heightTitle = height / 8.73f;
        titleTextSize = height / 24;
        radius = min(width, height) / 32;

        sensitivity = 1.5f;

        float buttonTextSize = getResources().getDimensionPixelSize(R.dimen.fontSize);

        btnClear = new CircleButton(width - radius * 8, heightTitle / 2, radius, "C", buttonTextSize);
        btnAbout = new CircleButton(width - radius * 5, heightTitle / 2, radius, "?", buttonTextSize);
        btnExit = new CircleButton(width - radius * 2, heightTitle / 2, radius, "X", buttonTextSize);

        //Log.d(mainActivity.DEBUG_TAG, width + ":" + height + ":" + radius);
        rotate(PI / 5, PI / 9);
    }

    private void initNodes() {
        int i = 0;
        for (int x = -1; x < 2; x++)
            for (int y = -1; y < 2; y++)
                for (int z = -1; z < 2; z++) {
                    nodes[i][0] = x;
                    nodes[i][1] = y;
                    nodes[i][2] = z;
                    nodes[i++][3] = 0;
                }
    }

    public void clearNodes() {
        for (double[] node : nodes) {
            node[3] = 0;
        }
    }

    public boolean changeColor(double x, double y) {
        x -= getWidth() / 2;
        y -= getHeight() / 2;

        for (int i = 0; i < nodes.length; i++)
            if (abs(nodes[i][0] - x) < radius * sensitivity && abs(nodes[i][1] - y) < radius * sensitivity) {
                if (nodes[i][3] == 0) {
                    nodes[i][3] = 1 - nodes[i][3];
                    return true;
                }
            }
        return false;
    }

    public void changeColorAI() {
        int i;
        do {
            i = random.nextInt(nodes.length);
        } while (nodes[i][3] != 0);
        nodes[i][3] = -1;
    }

    public boolean isCubeFill() {
        for (double[] node : nodes)
            if (node[3] == 0)
                return false;
        return true;
    }

    public boolean checkWin(int sign) {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                // checking Z axis
                if ((nodes[i*9 + j*3][3] + nodes[i*9+1 + j*3][3] + nodes[i*9+2 + j*3][3] == sign*3) ||
                        // checking Y axis
                        (nodes[i + j*9][3] + nodes[i + j*9 + 3][3] + nodes[i + j*9 + 6][3] == sign*3))
                    return true;
        // checking X axis
        for (int i = 0; i < 9; i++)
            if (nodes[i][3] + nodes[i + 9][3] + nodes[i + 18][3] == sign*3)
                return true;
        // checking diagonals of surfaces
        if ((nodes[0][3] + nodes[4][3] + nodes[8][3] == sign*3) ||
                (nodes[0][3] + nodes[10][3] + nodes[20][3] == sign*3) ||
                (nodes[0][3] + nodes[12][3] + nodes[24][3] == sign*3) ||
                (nodes[2][3] + nodes[4][3] + nodes[6][3] == sign*3) ||
                (nodes[2][3] + nodes[10][3] + nodes[18][3] == sign*3) ||
                (nodes[2][3] + nodes[14][3] + nodes[26][3] == sign*3) ||
                (nodes[6][3] + nodes[12][3] + nodes[18][3] == sign*3) ||
                (nodes[6][3] + nodes[16][3] + nodes[26][3] == sign*3) ||
                (nodes[8][3] + nodes[14][3] + nodes[20][3] == sign*3) ||
                (nodes[8][3] + nodes[16][3] + nodes[24][3] == sign*3) ||
                (nodes[18][3] + nodes[22][3] + nodes[26][3] == sign*3) ||
                (nodes[20][3] + nodes[22][3] + nodes[24][3] == sign*3))
            return true;
        // checking internal diagonals
        for (int i = 0; i < 9; i++)
            if (nodes[i][3] + nodes[13][3] + nodes[26 - i][3] == sign*3)
                return true;
        if ((nodes[9][3] + nodes[13][3] + nodes[17][3] == sign*3) ||
                (nodes[11][3] + nodes[13][3] + nodes[15][3] == sign*3))
            return true;
        return false;
    }

    private void scale(double s) {
        for (double[] node : nodes) {
            node[0] *= s;
            node[1] *= s;
            node[2] *= s;
        }
    }

    public void rotate(double angleX, double angleY) {
        double sinX = sin(angleX);
        double cosX = cos(angleX);

        double sinY = sin(angleY);
        double cosY = cos(angleY);

        for (double[] node : nodes) {
            double x = node[0];
            double y = node[1];
            double z = node[2];

            node[0] = x * cosX - z * sinX;
            node[2] = z * cosX + x * sinX;

            z = node[2];

            node[1] = y * cosY - z * sinY;
            node[2] = z * cosY + y * sinY;
        }
    }

    public boolean touchedClear(double x, double y) {
        return btnClear.isClick(x, y);
    }

    public boolean touchedAbout(double x, double y) {
        return btnAbout.isClick(x, y);
    }

    public boolean touchedExit(double x, double y) {
        return btnExit.isClick(x, y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        paint.setColor(Color.rgb(0x3f, 0x51, 0xb5));
        canvas.drawRect(0, 0, getWidth(), heightTitle, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(titleTextSize);
        canvas.drawText(getResources().getString(R.string.app_name), heightTitle / 3,
                (heightTitle - titleTextSize) / 2.3f + titleTextSize, paint);

        btnClear.draw(canvas, paint);
        btnAbout.draw(canvas, paint);
        btnExit.draw(canvas, paint);

        canvas.translate(getWidth() / 2, getHeight() / 2);

        paint.setColor(Color.WHITE);
        for (int[] edge : edges) {
            double[] xy1 = nodes[edge[0]];
            double[] xy2 = nodes[edge[1]];
            canvas.drawLine(round(xy1[0]), round(xy1[1]), round(xy2[0]), round(xy2[1]), paint);
        }

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        for (double[] node : nodes) {
            if (node[3] == 0) {
                paint.setColor(Color.WHITE);
                canvas.drawCircle(round(node[0]), round(node[1]), radius / 3, paint);
            } else {
                paint.setColor((node[3] < 0) ? Color.RED : Color.BLUE);
                canvas.drawCircle(round(node[0]), round(node[1]), radius, paint);
            }
        }
    }
}
