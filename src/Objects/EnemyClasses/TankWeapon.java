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
        boolean hasLineOfSight = e.hasLineOfSightTo(p);

        if (attacking) {
            attackElapsedTicks++;
            if (!attackDamageApplied && attackElapsedTicks >= getDamageDelayTicks(combo)) {
                if (hasLineOfSight && e.touchingOtherHitBox(attackArc, p.getHitbox())) {
                    p.hurt(getMoveDamage(combo));
                }
                attackDamageApplied = true;
            }
            if (attackElapsedTicks >= getAnimationTicks(combo)) {
                attacking = false;
                attackArc = null;
                combo = (combo + 1) % getComboLength();
                lastAttack = tick;
            }
        } else {
            double attackAngle = Math.atan2(dy, dx);
            Arc2D aimedArc = createAttackArc(e, attackAngle,
                    getMoveRange(combo), getMoveAngle(combo));
            boolean targetInRange = e.touchingOtherHitBox(aimedArc, p.getHitbox());
            if (ready(tick) && hasLineOfSight && targetInRange) {
                attackElapsedTicks = 0;
                attackDamageApplied = false;
                attackArc = aimedArc;
                attacking = true;
            } else if (!hasLineOfSight || !targetInRange) {
                if (distance > 0) {
                    double step = Math.min(e.getCurrentSpeed(tick), distance);
                    e.moveWithWallCollision((int) Math.round(dx / distance * step),
                            (int) Math.round(dy / distance * step));
                }
            }
        }
    }

    public void cancelAttack() {
        attacking = false;
        attackArc = null;
        attackElapsedTicks = 0;
        attackDamageApplied = false;
    }

    @Override
    public void aim(Enemy e, Player p, Graphics2D g) {
        if (attacking && attackArc != null) {
            g.setColor(attackDamageApplied
                    ? new Color(255, 60, 60, 190)
                    : new Color(255, 255, 255, 170));
            g.fill(attackArc);
            g.setColor(new Color(255, 255, 255, 220));
            g.draw(attackArc);
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

    protected int getComboLength() {
        return RANGES.length;
    }

    protected int getMoveRange(int move) {
        return RANGES[move];
    }

    protected int getMoveAngle(int move) {
        return ANGLES[move];
    }

    protected int getAnimationTicks(int move) {
        return ANIMATION_TICKS[move];
    }

    protected int getDamageDelayTicks(int move) {
        return DAMAGE_DELAY_TICKS[move];
    }

    protected int getMoveDamage(int move) {
        return DAMAGES[move];
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
