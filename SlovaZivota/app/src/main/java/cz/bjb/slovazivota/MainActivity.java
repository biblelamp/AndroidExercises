package cz.bjb.slovazivota;

/**
 * Java. Slova Života - Rozjímejte nad Božím Slovem
 *
 * @author Sergey Iryupin
 * @version 0.4.6 dated Jan 01, 2019
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements
        OnGestureListener, OnDoubleTapListener {

    private TextView textView;
    private DateTool date;
    private TextTool text;
    private float textSize;

    //private static final String DEBUG_TAG = "Gestures";
    private GestureDetectorCompat gdc;

    private static final String APP_SETTINGS = "settings";
    private static final String APP_SETTINGS_TEXTSIZE = "textsize";
    private static final float APP_TEXTSIZE_DEFAULT = 16;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);

        gdc = new GestureDetectorCompat(this,this);
        gdc.setOnDoubleTapListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        date = new DateTool();
        text = new TextTool(this);

        textView = (TextView) findViewById(R.id.text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gdc.onTouchEvent(event);
                return false;
            }
        });

        updateView();
    }

    private void updateView() {
        setTitle("  " + date.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(text.getFileFromAsset(date.getFileName()),
                    Html.FROM_HTML_MODE_LEGACY));
        } else {
            textView.setText(Html.fromHtml(text.getFileFromAsset(date.getFileName())));
        }
        textView.scrollTo(0, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(APP_SETTINGS_TEXTSIZE, textSize);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (settings.contains(APP_SETTINGS_TEXTSIZE)) {
            textSize = settings.getFloat(APP_SETTINGS_TEXTSIZE, 0);
        } else {
            textSize = APP_TEXTSIZE_DEFAULT;
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        getSupportActionBar().setIcon(R.drawable.snowflake);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.font_increase:
                textSize += 2;
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                return true;
            case R.id.font_reduce:
                textSize -= 2;
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                return true;
            case R.id.about:
                Builder builder = new Builder(MainActivity.this);
                builder.setTitle(R.string.app_name)
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage(getString(R.string.app_description) + "\n" +
                                getString(R.string.app_texts_from) + "\n" +
                                getString(R.string.app_developer))
                        .setCancelable(false)
                        .setNegativeButton(R.string.btn_ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (this.gdc.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        //Log.d(DEBUG_TAG,"onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        //Log.d(DEBUG_TAG,"onFling: " + velocityX + ":" + velocityY);
        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            date.add((int) Math.signum(-velocityX));
            updateView();
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX,
                            float distanceY) {
        //Log.d(DEBUG_TAG, "onScroll: " + event1.toString() + event2.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        date.setDate(new Date());
        updateView();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        return true;
    }
}
