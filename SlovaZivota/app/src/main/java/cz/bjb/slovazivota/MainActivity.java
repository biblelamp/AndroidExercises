package cz.bjb.slovazivota;

/**
 * Java. Slova Života - přemýšlejte o Božím Slovu
 *
 * @author Sergey Iryupin
 * @version 0.2 dated Nov 26, 2018
 */

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private DateTool date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        date = new DateTool();
        setTitle(date.toString());

        TextView textView = (TextView) findViewById(R.id.text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText(getStringFromAssetFile());
    }

    private String getStringFromAssetFile() {
        byte[] buffer = null;
        try {
            InputStream is = getAssets().open(date.getFileName());
            buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new String(buffer);
    }
}
