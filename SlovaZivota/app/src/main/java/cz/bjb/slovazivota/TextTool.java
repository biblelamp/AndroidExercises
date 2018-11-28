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
        return textToHTML(buffer);
    }

    private String textToHTML(byte[] buffer) {
        String[] lines = new String(buffer).split("\n");
        StringBuffer sb = new StringBuffer();
        int counter = 0;
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].isEmpty()) {
                counter++;
                if (counter == 1) {
                    lines[i] = wrapStringInTags(lines[i], "b");
                } else if (counter == 2 || i == lines.length - 1) {
                    lines[i] = wrapStringInTags(lines[i], "i");
                }
                sb.append(wrapStringInTags(lines[i], "p"));
            }
        }
        return sb.toString();
    }

    private String wrapStringInTags(String str, String tag) {
        return "<" + tag + ">" + str + "</" + tag + ">";
    }
}
