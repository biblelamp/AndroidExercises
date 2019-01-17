package eu.javageek.t3d;

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
 * Java. Tic tac toe 3D
 *
 * @author Sergey Iryupin
 * @version 0.1.1 dated Jan 17, 2019
 */

public class MainActivity extends Activity implements OnTouchListener {

    public static final String DEBUG_TAG = "T3D";

    private DrawView drawView;
    private float mouseX, prevMouseX, mouseY, prevMouseY;
    private boolean gameOver = false;

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

                if (!gameOver) {
                    if (drawView.changeColor(mouseX, mouseY)) {
                        drawView.invalidate();
                        if (drawView.checkWin(1)) {
                            showAlert(R.string.you_won);
                            gameOver = true;
                        } else if (drawView.isCubeFill()) {
                            showAlert(R.string.sorry_draw);
                            gameOver = true;
                        }
                        if (!gameOver) {
                            drawView.changeColorAI();
                            drawView.invalidate();
                            if (drawView.checkWin(-1)) {
                                showAlert(R.string.ai_won);
                                gameOver = true;
                            } else if (drawView.isCubeFill()) {
                                showAlert(R.string.sorry_draw);
                                gameOver = true;
                            }
                        }
                    }
                }

                if (drawView.touchedClear(mouseX, mouseY)) {
                    gameOver = false;
                    drawView.clearNodes();
                    drawView.invalidate();
                }

                if (drawView.touchedAbout(mouseX, mouseY)) {
                    showAlert(R.string.app_description);
                }

                if (drawView.touchedExit(mouseX, mouseY)) {
                    System.exit(0);
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
