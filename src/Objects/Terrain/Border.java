package Objects.Terrain;

import Objects.VisualObject;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class Border extends VisualObject {
    public Border(int x, int y, int width, int height, Color color) {
        super(x, y, width, height, color);
    }

    @Override
    public void draw(Graphics g) {
        if (!this.visible) return;
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color base = this.color != null ? this.color : new Color(55, 60, 75);
            // base
            g2.setColor(base);
            g2.fillRect(this.x, this.y, this.width, this.height);
            // top/left highlight
            g2.setColor(brighten(base, 28));
            g2.fillRect(this.x, this.y, this.width, Math.min(3, this.height));
            g2.fillRect(this.x, this.y, Math.min(3, this.width), this.height);
            // bottom/right shadow
            g2.setColor(darken(base, 36));
            g2.fillRect(this.x, this.y + Math.max(0, this.height - 3), this.width, Math.min(3, this.height));
            g2.fillRect(this.x + Math.max(0, this.width - 3), this.y, Math.min(3, this.width), this.height);
            // inner bevel line
            g2.setColor(darken(base, 18));
            g2.drawRect(this.x + 1, this.y + 1, Math.max(0, this.width - 3), Math.max(0, this.height - 3));
            // subtle rune dot every ~48px for texture (stable by position)
            g2.setColor(new Color(120, 140, 200, 60));
            int step = 48;
            for (int rx = this.x + 8; rx < this.x + this.width - 8; rx += step) {
                for (int ry = this.y + 8; ry < this.y + this.height - 8; ry += step) {
                    g2.fillOval(rx, ry, 2, 2);
                }
            }
        } finally {
            g2.dispose();
        }
    }

    private static Color brighten(Color c, int amt) {
        return new Color(Math.min(255, c.getRed() + amt), Math.min(255, c.getGreen() + amt), Math.min(255, c.getBlue() + amt));
    }

    private static Color darken(Color c, int amt) {
        return new Color(Math.max(0, c.getRed() - amt), Math.max(0, c.getGreen() - amt), Math.max(0, c.getBlue() - amt));
    }
}
