package cz.bjb.slovazivota;

/**
 * Java. Slova Života - přemýšlejte o Božím Slovu
 *
 * @author Sergey Iryupin
 * @version 0.2.1 dated Nov 26, 2018
 */

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private TextView textView;
    private DateTool date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        date = new DateTool();
        setTitle(date.toString());

        textView = (TextView) findViewById(R.id.text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setOnTouchListener(this);

        textView.setText(getStringFromAssetFile());
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float x = 0;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() > textView.getWidth()/2) {
                    date.add(1);
                } else {
                    date.add(-1);
                }
                setTitle(date.toString());
                textView.setText(getStringFromAssetFile());
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    private String getStringFromAssetFile() {
        InputStream is = null;
        byte[] buffer = null;
        try {
            is = getAssets().open(date.getFileName());
            buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
        } catch (IOException ex) {
            return "Žádný text pro tento den.";
        }
        return new String(buffer);
    }
}
