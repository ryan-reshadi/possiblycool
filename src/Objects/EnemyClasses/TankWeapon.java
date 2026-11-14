package Objects.EnemyClasses;

import Objects.PlayerClasses.Player;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

/** Slow, clearly telegraphed melee swings with a three move cadence. */
public class TankWeapon extends EnemyWeapon {
    private int combo;
    private int windup;
    private double lockedAngle;
    private final int[] ranges = {58, 68, 82};
    private final int[] angles = {45, 70, 105};
    private final int[] delays = {39, 48, 57};
    private final int[] damages = {10, 12, 17};
    public TankWeapon() { super(12, 105, 70, 70); }
    @Override public void update(Enemy e, Player p, int tick) {
        if (p == null) return;
        double dx = p.getX() + p.getWidth()/2.0 - (e.x + e.width/2.0);
        double dy = p.getY() + p.getHeight()/2.0 - (e.y + e.height/2.0);
        if (windup > 0) {
            if (--windup == 0) {
                double r = ranges[combo], half = angles[combo] / 2.0;
                Arc2D arc = new Arc2D.Double(e.x + e.width/2.0-r, e.y+e.height/2.0-r, 2*r, 2*r,
                        -Math.toDegrees(lockedAngle)-half, angles[combo], Arc2D.PIE);
                Rectangle2D target = p.getHitbox();
                if (arc.intersects(target) || arc.contains(target.getCenterX(), target.getCenterY())) p.hurt(damages[combo]);
                combo = (combo + 1) % 3;
                lastAttack = tick;
            }
        } else if (ready(tick) && Math.hypot(dx, dy) < range) {
            lockedAngle = Math.atan2(dy, dx);
            windup = delays[combo];
        } else if (Math.hypot(dx, dy) > range) {
            double d = Math.hypot(dx, dy), step = Math.min(0.7, d);
            e.moveWithWallCollision((int)Math.round(dx/d*step), (int)Math.round(dy/d*step));
        }
    }
    @Override public void aim(Enemy e, Player p, java.awt.Graphics2D g) {
        if (p == null) return;
        double a = windup > 0 ? lockedAngle : Math.atan2(p.getY()-e.y,p.getX()-e.x);
        g.setColor(Color.DARK_GRAY); g.setStroke(new java.awt.BasicStroke(6));
        g.drawLine(e.x+e.width/2,e.y+e.height/2,(int)(e.x+e.width/2+Math.cos(a)*28),(int)(e.y+e.height/2+Math.sin(a)*28));
        g.setStroke(new java.awt.BasicStroke(1));
    }
}
