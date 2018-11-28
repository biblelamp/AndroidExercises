package cz.bjb.slovazivota;

import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;

public class TextTool {
    private Activity activity;

    public TextTool(Activity activity) {
        this.activity = activity;
    }

    public String getStringFromAssetFile(String fileName) {
        InputStream is = null;
        byte[] buffer = null;
        try {
            is = activity.getAssets().open(fileName);
            buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
        } catch (IOException ex) {
            return activity.getString(R.string.no_text);
        }
        return new String(buffer);
    }
}
