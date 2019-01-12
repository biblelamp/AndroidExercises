package eu.javageek.cube3d;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.*;

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
                    drawView.clearNodes();
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

                drawView.rotate(incrX, incrY);
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
}
