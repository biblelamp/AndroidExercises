package ru.rostov.a1_l1;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //TODO-shv: my first application in this course works successfully
    // Ctrl + O for Override

    TextView txt_hello;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        Log.d(getClass().getSimpleName(), "onCreate worked");

        TextView txt_hello = (TextView) findViewById(R.id.textView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(getClass().getSimpleName(), "onDestroy worked");
    }

    public void onBtnClick(View view) {
        txt_hello.setTextColor(Color.RED);
    }
}
