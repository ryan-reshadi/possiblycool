package Levels.Generation;

/** Axis-aligned integer rectangle used for layout geometry. */
public final class Rect {
    public final int x;
    public final int y;
    public final int w;
    public final int h;

    public Rect(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public int left() { return x; }
    public int top() { return y; }
    public int right() { return x + w; }
    public int bottom() { return y + h; }
    public int centerX() { return x + w / 2; }
    public int centerY() { return y + h / 2; }

    public boolean intersects(Rect other) {
        return this.x < other.right() && this.right() > other.x
                && this.y < other.bottom() && this.bottom() > other.y;
    }

    public Rect inset(int amount) {
        int nw = Math.max(1, w - amount * 2);
        int nh = Math.max(1, h - amount * 2);
        return new Rect(x + amount, y + amount, nw, nh);
    }
}
