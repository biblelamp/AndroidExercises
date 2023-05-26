package cz.android;

public class Button {
    private final float x, y, r;

    public Button(float x, float y, float r) {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getR() {
        return r;
    }

    public boolean isClick(float x2, float y2) {
        double d = Math.sqrt(Math.pow(x - x2, 2) + Math.pow(y - y2, 2));
        return r >= d;
    }
}
