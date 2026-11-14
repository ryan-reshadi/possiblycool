package Objects.EnemyClasses;

import Objects.PlayerClasses.Player;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;

/** Tank enemies use a three-swing combo modeled after the player's TankSword. */
public class TankWeapon extends EnemyWeapon {
    private static final int[] RANGES = {72, 88, 104};
    private static final int[] ANGLES = {44, 62, 88};
    private static final int[] ANIMATION_TICKS = {19, 25, 32};
    private static final int[] DAMAGE_DELAY_TICKS = {7, 10, 15};
    private static final int[] DAMAGES = {10, 12, 17};

    private int combo;
    private int attackElapsedTicks;
    private boolean attacking;
    private boolean attackDamageApplied;
    private Arc2D attackArc;

    public TankWeapon() {
        super(12, 105, 70, 70);
    }

    @Override
    public void update(Enemy e, Player p, int tick) {
        if (p == null) return;

        double dx = p.getX() + p.getWidth() / 2.0 - (e.x + e.width / 2.0);
        double dy = p.getY() + p.getHeight() / 2.0 - (e.y + e.height / 2.0);
        double distance = Math.hypot(dx, dy);

        if (attacking) {
            attackElapsedTicks++;
            if (!attackDamageApplied && attackElapsedTicks >= DAMAGE_DELAY_TICKS[combo]) {
                if (e.hasLineOfSightTo(p) && e.touchingOtherHitBox(attackArc, p.getHitbox())) {
                    p.hurt(DAMAGES[combo]);
                }
                attackDamageApplied = true;
            }
            if (attackElapsedTicks >= ANIMATION_TICKS[combo]) {
                attacking = false;
                attackArc = null;
                combo = (combo + 1) % RANGES.length;
                lastAttack = tick;
            }
        } else if (ready(tick) && distance < range && e.hasLineOfSightTo(p)) {
            double attackAngle = Math.atan2(dy, dx);
            attackElapsedTicks = 0;
            attackDamageApplied = false;
            attackArc = createAttackArc(e, attackAngle, RANGES[combo], ANGLES[combo]);
            attacking = true;
        } else if (distance > range) {
            double step = Math.min(0.7, distance);
            e.moveWithWallCollision((int) Math.round(dx / distance * step),
                    (int) Math.round(dy / distance * step));
        }
    }

    @Override
    public void aim(Enemy e, Player p, Graphics2D g) {
        if (attacking && attackArc != null) {
            g.setColor(attackDamageApplied
                    ? new Color(255, 60, 60, 160)
                    : new Color(255, 255, 255, 120));
            g.fill(attackArc);
            return;
        }
        if (p == null) return;

        double angle = Math.atan2(p.getY() + p.getHeight() / 2.0 - (e.y + e.height / 2.0),
                p.getX() + p.getWidth() / 2.0 - (e.x + e.width / 2.0));
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new java.awt.BasicStroke(6));
        g.drawLine(e.x + e.width / 2, e.y + e.height / 2,
                (int) (e.x + e.width / 2 + Math.cos(angle) * 28),
                (int) (e.y + e.height / 2 + Math.sin(angle) * 28));
        g.setStroke(new java.awt.BasicStroke(1));
    }

    public Arc2D getAttackArc() {
        return attackArc;
    }

    public boolean isAttacking() {
        return attacking;
    }

    private static Arc2D createAttackArc(Enemy e, double angle, int attackRange, int attackAngle) {
        double centerX = e.x + e.width / 2.0;
        double centerY = e.y + e.height / 2.0;
        return new Arc2D.Double(centerX - attackRange, centerY - attackRange,
                attackRange * 2.0, attackRange * 2.0,
                -Math.toDegrees(angle) - attackAngle / 2.0,
                attackAngle, Arc2D.PIE);
    }
}
