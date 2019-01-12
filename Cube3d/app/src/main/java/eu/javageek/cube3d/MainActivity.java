package eu.javageek.cube3d;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
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
 * @version 0.0.14 dated Jan 12, 2019
 */

public class MainActivity extends Activity implements OnTouchListener {

    public static final String DEBUG_TAG = "Cube3D";

    private DrawView drawView;
    private float mouseX, prevMouseX, mouseY, prevMouseY;

    public double[][] nodes = {{-1, -1, -1, 0}, {-1, -1, 1, 0}, {-1, 1, -1, 0}, {-1, 1, 1, 0},
            {1, -1, -1, 0}, {1, -1, 1, 0}, {1, 1, -1, 0}, {1, 1, 1, 0}};

    public int[][] edges = {{0, 1}, {1, 3}, {3, 2}, {2, 0}, {4, 5}, {5, 7}, {7, 6},
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

                if (drawView.touchedClear(mouseX, mouseY)) {
                    for (double[] node : nodes) {
                        node[3] = 0;
                    }
                    drawView.invalidate();
                }

                if (drawView.touchedAbout(mouseX, mouseY)) {
                    showAlert(R.string.app_description);
                }

                if (drawView.touchedExit(mouseX, mouseY)) {
                    System.exit(0);
                }

                if (drawView.changeColor(mouseX, mouseY)) {
                    drawView.invalidate();
                }

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

    private void showAlert(int message) {
        Builder builder = new Builder(MainActivity.this);
        builder.setTitle(R.string.app_name)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(message)
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

    public void scale(double s) {
        for (double[] node : nodes) {
            node[0] *= s;
            node[1] *= s;
            node[2] *= s;
        }
    }

    public void rotateCube(double angleX, double angleY) {
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
}
