package Objects.PlayerEquipment;

import Objects.VisualObject;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

public class MageBall extends VisualObject {
    private double angle;
    private int speed;
    private double falloff = 0;

    public MageBall(double angle, int speed, double falloff, int x, int y) {
        super(x, y, 14, 14, (Color) null);
        this.angle = angle;
        this.speed = speed;
        this.falloff = falloff;
        this.setImgPath(null);
    }

    public MageBall(double angle, int speed, int x, int y) {
        super(x, y, 14, 14, (Color) null);
        this.angle = angle;
        this.speed = speed;
        this.setImgPath(null);
    }

    public void tick() {
        this.x += Math.cos(angle) * speed;
        this.y += Math.sin(angle) * speed;
        this.speed -= falloff;
    }

    public void remove(ArrayList<VisualObject> objects) {
        objects.remove(this);
        this.visible = false;
    }

    @Override
    public void draw(Graphics g) {
        if (!this.visible) return;
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = this.x + this.width / 2;
            int cy = this.y + this.height / 2;
            // halo
            g2.setColor(new Color(140, 200, 255, 60));
            g2.fillOval(cx - 10, cy - 10, 20, 20);
            g2.setColor(new Color(120, 170, 255, 90));
            g2.fillOval(cx - 7, cy - 7, 14, 14);
            // core
            g2.setColor(new Color(220, 240, 255));
            g2.fillOval(cx - 4, cy - 4, 8, 8);
            g2.setColor(new Color(120, 200, 255));
            g2.fillOval(cx - 2, cy - 2, 4, 4);
        } finally {
            g2.dispose();
        }
    }
}
