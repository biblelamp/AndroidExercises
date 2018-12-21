package cz.javageek.cube3d;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.*;

import static java.lang.Math.*;

/**
 * Java. Cube 3D
 *
 * @author Sergey Iryupin
 * @version 0.0.2 dated Dec 21, 2018
 */

public class MainActivity extends AppCompatActivity implements OnTouchListener {

    private static final String DEBUG_TAG = "Cube3D";

    DrawView drawView;
    float mouseX, prevMouseX, mouseY, prevMouseY;

    double[][] nodes = {{-1, -1, -1}, {-1, -1, 1}, {-1, 1, -1}, {-1, 1, 1},
            {1, -1, -1}, {1, -1, 1}, {1, 1, -1}, {1, 1, 1}};

    int[][] edges = {{0, 1}, {1, 3}, {3, 2}, {2, 0}, {4, 5}, {5, 7}, {7, 6},
            {6, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawView = new DrawView(this);
        drawView.setOnTouchListener(this);
        setContentView(drawView);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mouseX = event.getX();
                mouseY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                prevMouseX = mouseX;
                prevMouseY = mouseY;
                mouseX = event.getX();
                mouseY = event.getY();

                double incrX = (mouseX - prevMouseX) * 0.01;
                double incrY = (mouseY - prevMouseY) * 0.01;

                rotateCube(incrX, incrY);
                drawView.invalidate();

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    private void scale(double s) {
        for (double[] node : nodes) {
            node[0] *= s;
            node[1] *= s;
            node[2] *= s;
        }
    }

    private void rotateCube(double angleX, double angleY) {
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

    class DrawView extends View {

        Paint paint;

        public DrawView(Context context) {
            super(context);
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            scale(min(width, height) / 4);

            //Log.d(DEBUG_TAG, width + ":" + height);
            rotateCube(PI / 5, PI / 9);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            canvas.translate(getWidth() / 2, getHeight() / 2);
            canvas.drawColor(Color.BLACK);
            paint.setColor(Color.WHITE);

            for (int[] edge : edges) {
                double[] xy1 = nodes[edge[0]];
                double[] xy2 = nodes[edge[1]];
                canvas.drawLine(round(xy1[0]), round(xy1[1]), round(xy2[0]), round(xy2[1]), paint);
            }

            for (double[] node : nodes)
                canvas.drawCircle(round(node[0]), round(node[1]),4, paint);
        }
    }
}
