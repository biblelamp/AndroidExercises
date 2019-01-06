package cz.javageek.cube3d;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.*;

import static java.lang.Math.*;

/**
 * Java. Cube 3D
 *
 * @author Sergey Iryupin
 * @version 0.0.9 dated Jan 06, 2019
 */

public class MainActivity extends Activity implements OnTouchListener {

    private static final String DEBUG_TAG = "Cube3D";

    private DrawView drawView;
    private float mouseX, prevMouseX, mouseY, prevMouseY;

    private double[][] nodes = {{-1, -1, -1, 0}, {-1, -1, 1, 0}, {-1, 1, -1, 0}, {-1, 1, 1, 0},
            {1, -1, -1, 0}, {1, -1, 1, 0}, {1, 1, -1, 0}, {1, 1, 1, 0}};

    private int[][] edges = {{0, 1}, {1, 3}, {3, 2}, {2, 0}, {4, 5}, {5, 7}, {7, 6},
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

                if (drawView.touchedAbout(mouseX, mouseY)) {
                    Builder builder = new Builder(MainActivity.this);
                    builder.setTitle(R.string.app_name)
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage(R.string.app_description)
                            .setCancelable(false)
                            .setNegativeButton(R.string.btn_ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

                if (drawView.touchedExit(mouseX, mouseY))
                    System.exit(0);

                if (drawView.changeColor(mouseX, mouseY))
                    drawView.invalidate();

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
        int radius;
        CircleButton btnExit;

        public DrawView(Context context) {
            super(context);
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();

            scale(min(width, height) / 4);

            radius = min(width, height) / 32;

            btnExit = new CircleButton(width - 20, 28, 12, "X", width - 23, 33);

            //Log.d(DEBUG_TAG, width + ":" + height);
            rotateCube(PI / 5, PI / 9);
        }

        private boolean changeColor(double x, double y) {
            x -= getWidth() / 2;
            y -= getHeight() / 2;

            for (int i = 0; i < nodes.length; i++)
                if (abs(nodes[i][0] - x) < radius*2 && abs(nodes[i][1] - y) < radius*2) {
                    nodes[i][3] = 1 - nodes[i][3];
                    return true;
                }
            return false;
        }

        private boolean touchedAbout(double x, double y) {
            return (abs(getWidth() - 52 - x) < 12 && abs(28 - y) < 12);
        }

        private boolean touchedExit(double x, double y) {
            return (abs(getWidth() - 20 - x) < 12 && abs(28 - y) < 12);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.BLACK);

            paint.setColor(Color.rgb(0x3f, 0x51, 0xb5));
            canvas.drawRect(0, 0, getWidth(), 55, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(20);
            canvas.drawText("Cube 3D", 18, 35, paint);

            btnExit.draw(canvas, paint);

            paint.setColor(Color.WHITE);
            //canvas.drawCircle(getWidth() - 20, 28, 12, paint);
            canvas.drawCircle(getWidth() - 52, 28, 12, paint);

            paint.setColor(Color.BLACK);
            paint.setTextSize(12);
            //canvas.drawText("X", getWidth() - 23, 33, paint);
            canvas.drawText("?", getWidth() - 55, 33, paint);

            canvas.translate(getWidth() / 2, getHeight() / 2);

            paint.setColor(Color.WHITE);
            for (int[] edge : edges) {
                double[] xy1 = nodes[edge[0]];
                double[] xy2 = nodes[edge[1]];
                canvas.drawLine(round(xy1[0]), round(xy1[1]), round(xy2[0]), round(xy2[1]), paint);
            }

            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            for (double[] node : nodes) {
                paint.setColor((node[3] == 0)? Color.WHITE : Color.RED);
                canvas.drawCircle(round(node[0]), round(node[1]), radius, paint);
            }
        }
    }
}
