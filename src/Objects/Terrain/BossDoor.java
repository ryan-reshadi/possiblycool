package Objects.Terrain;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class BossDoor extends Border {

    private boolean locked = true;
    private final Runnable onUnlock;

    public BossDoor(int x, int y, int width, int height, Runnable onUnlock) {
        super(x, y, width, height, Color.RED);
        this.onUnlock = onUnlock;
    }

    public boolean isLocked() {
        return locked;
    }

    public void unlock() {
        this.locked = false;
        this.moveable = true;
        this.color = new Color(50, 50, 50);
        if (onUnlock != null) {
            onUnlock.run();
        }
    }

    @Override
    public void draw(Graphics g) {
        if (locked) {
            g.setColor(new Color(180, 40, 40));
            g.fillRect(x, y, width, height);
            g.setColor(Color.WHITE);
            g.drawRect(x, y, width, height);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("BOSS DOOR [LOCKED]", x + 10, y + height / 2 + 5);
        } else {
            g.setColor(new Color(60, 60, 70));
            g.fillRect(x, y, width, height);
            g.setColor(Color.GREEN);
            g.drawRect(x, y, width, height);
            g.drawString("OPEN", x + 10, y + height / 2 + 5);
        }
    }
}
