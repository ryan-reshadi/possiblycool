package Objects.Effects;

import Objects.VisualObject;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Visual explosion effect that expands and fades out.
 */
public class ArcaneExplosionEffect extends VisualObject {

    private int ticksAlive = 0;
    private final int maxTicks;
    private final Runnable onExpire;

    public ArcaneExplosionEffect(int centerX, int centerY, int radius, Runnable onExpire) {
        super(centerX - radius, centerY - radius, radius * 2, radius * 2, Color.MAGENTA);
        this.maxTicks = 20;
        this.onExpire = onExpire;
    }

    @Override
    public void update(int tickCount) {
        ticksAlive++;
        if (ticksAlive >= maxTicks) {
            this.visible = false;
            if (onExpire != null) {
                onExpire.run();
            }
        }
    }

    @Override
    public void render(Graphics2D g, int tickCount) {
        draw(g);
    }

    @Override
    public void draw(Graphics g) {
        if (!visible) return;
        Graphics2D g2d = (Graphics2D) g;
        Composite oldComp = g2d.getComposite();
        float alpha = Math.max(0.0f, 1.0f - ((float) ticksAlive / maxTicks));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.7f));
        g2d.setColor(Color.MAGENTA);
        g2d.fillOval(x, y, width, height);
        g2d.setColor(Color.WHITE);
        g2d.drawOval(x, y, width, height);
        g2d.setComposite(oldComp);
    }
}
