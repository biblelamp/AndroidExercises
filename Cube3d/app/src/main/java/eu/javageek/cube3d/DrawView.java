package eu.javageek.cube3d;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.Display;
import android.view.View;

import static java.lang.Math.*;

public class DrawView extends View {

    MainActivity mainActivity;
    Paint paint;
    CircleButton btnClear, btnAbout, btnExit;
    float heightTitle;
    float titleTextSize;
    float radius;

    public DrawView(MainActivity mainActivity) {
        super(mainActivity);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        this.mainActivity = mainActivity;

        Display display = mainActivity.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        mainActivity.scale(min(width, height) / 4);

        heightTitle = height / 8.73f;
        titleTextSize = height / 24;
        radius = min(width, height) / 32;

        btnClear = new CircleButton(width - radius * 8, heightTitle /2, radius, "C", -4, 5);
        btnAbout = new CircleButton(width - radius * 5, heightTitle / 2, radius, "?", -3, 5);
        btnExit = new CircleButton(width - radius * 2, heightTitle / 2, radius, "X", -3, 5);

        //Log.d(mainActivity.DEBUG_TAG, width + ":" + height + ":" + radius);
        mainActivity.rotateCube(PI / 5, PI / 9);
    }

    public boolean changeColor(double x, double y) {
        x -= getWidth() / 2;
        y -= getHeight() / 2;

        for (int i = 0; i < mainActivity.nodes.length; i++)
            if (abs(mainActivity.nodes[i][0] - x) < radius*2 && abs(mainActivity.nodes[i][1] - y) < radius*2) {
                mainActivity.nodes[i][3] = 1 - mainActivity.nodes[i][3];
                return true;
            }
        return false;
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
        canvas.drawText("Cube 3D", heightTitle / 3,
                (heightTitle - titleTextSize) / 2.3f + titleTextSize, paint);

        btnClear.draw(canvas, paint);
        btnAbout.draw(canvas, paint);
        btnExit.draw(canvas, paint);

        canvas.translate(getWidth() / 2, getHeight() / 2);

        paint.setColor(Color.WHITE);
        for (int[] edge : mainActivity.edges) {
            double[] xy1 = mainActivity.nodes[edge[0]];
            double[] xy2 = mainActivity.nodes[edge[1]];
            canvas.drawLine(round(xy1[0]), round(xy1[1]), round(xy2[0]), round(xy2[1]), paint);
        }

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        for (double[] node : mainActivity.nodes) {
            paint.setColor((node[3] == 0)? Color.WHITE : Color.RED);
            canvas.drawCircle(round(node[0]), round(node[1]), radius, paint);
        }
    }
}
