package Objects.PlayerEquipment;

import Objects.Animations.Animation;
import Objects.Animations.AnimationSegment;
import Objects.EnemyClasses.Enemy;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public abstract class MeleeWeapon extends Item {

    protected static class AttackMove {
        final int range;
        final int angle;
        final int animationTicks;
        final int damageDelayTicks;
        final double damageMultiplier;
        final double knockbackMultiplier;

        protected AttackMove(int range, int angle, int animationTicks, int damageDelayTicks,
                             double damageMultiplier, double knockbackMultiplier) {
            this.range = range;
            this.angle = angle;
            this.animationTicks = animationTicks;
            this.damageDelayTicks = damageDelayTicks;
            this.damageMultiplier = damageMultiplier;
            this.knockbackMultiplier = knockbackMultiplier;
        }
    }

    protected Animation attackAnimation;
    protected int attackAnimationLength = this.secondsToTicks(0.6);
    protected int attackDamageDelay = this.secondsToTicks(0.2);
    protected int attackRange = 100;
    protected int attackAngle = 30;
    protected int damage = 25;
    protected double knockbackForce = 5.0;
    protected int attackSourceX;
    protected int attackSourceY;
    protected int attackTick;
    protected Arc2D attackBox;
    protected boolean attackDamageApplied = false;
    protected List<Enemy> enemies;
    private AttackMove[] comboMoves;
    private int comboIndex;
    private AttackMove activeMove;

    public MeleeWeapon(int attackRange, int attackAngle, int damage) {
        this.attackRange = attackRange;
        this.attackAngle = attackAngle;
        this.damage = damage;
    }

    public MeleeWeapon(int attackRange, int attackAngle) {
        this(attackRange, attackAngle, 25);
    }

    public void setEnemies(List<Enemy> enemies) {
        this.enemies = enemies;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    protected final AttackMove move(int range, int angle, double durationSeconds, double damageDelaySeconds,
                                    double damageMultiplier, double knockbackMultiplier) {
        return new AttackMove(range, angle, secondsToTicks(durationSeconds),
                secondsToTicks(damageDelaySeconds), damageMultiplier, knockbackMultiplier);
    }

    protected final void setCombo(AttackMove... moves) {
        if (moves == null || moves.length == 0) throw new IllegalArgumentException("A melee combo needs at least one move.");
        this.comboMoves = moves.clone();
        this.comboIndex = 0;
    }

    public void attack(int clickXDown, int clickYDown, int currentTick, int processedX, int processedY) {
        if (isInAnimation()) return;

        activeMove = (comboMoves == null)
                ? new AttackMove(attackRange, attackAngle, attackAnimationLength, attackDamageDelay, 1.0, 1.0)
                : comboMoves[comboIndex];

        double deltaX = -1 * (processedX - clickXDown);
        double deltaY = -1 * (processedY - clickYDown);

        this.attackSourceX = processedX;
        this.attackSourceY = processedY;
        this.attackTick = currentTick;

        double angleRadians = Math.atan2(deltaY, deltaX);
        double angleDegrees = Math.toDegrees(angleRadians);

        this.attackBox = new Arc2D.Double(
                processedX - activeMove.range,
                processedY - activeMove.range,
                activeMove.range * 2,
                activeMove.range * 2,
                -1 * (angleDegrees + (activeMove.angle / 2.0)),
                activeMove.angle,
                Arc2D.PIE
        );

        this.startAttackAnimation();
    }

    public void startAttackAnimation() {
        if (attackAnimation != null && attackAnimation.isPlaying()) {
            return;
        }

        attackDamageApplied = false;
        attackAnimation = new Animation();

        AnimationSegment swingSegment = new AnimationSegment(
                0,
                activeMove.animationTicks,
                () -> {},
                () -> {},
                null
        );

        AnimationSegment damageSegment = new AnimationSegment(
                activeMove.damageDelayTicks,
                1,
                () -> applyAttackDamage(),
                null,
                null
        );

        attackAnimation.addSegment(swingSegment);
        attackAnimation.addSegment(damageSegment);
        attackAnimation.addEndAction(() -> onAttackAnimationEnd());

        attackAnimation.play();
    }

    protected void applyAttackDamage() {
        if (!attackDamageApplied) {
            attackDamageApplied = true;
            if (enemies != null && attackBox != null) {
                for (Enemy enemy : enemies) {
                    if (enemy != null && !enemy.isDead() && enemy.getHitbox() != null) {
                        if (touchingOtherHitBox(attackBox, enemy.getHitbox())) {
                            int attackDamage = Math.max(1, (int) Math.round(damage * activeMove.damageMultiplier));
                            enemy.hit(attackDamage, attackSourceX, attackSourceY,
                                    knockbackForce * activeMove.knockbackMultiplier, attackTick);
                        }
                    }
                }
            }
        }
    }

    protected void onAttackAnimationEnd() {
        attackBox = null;
        resetAnimation();
        if (comboMoves != null) {
            comboIndex = (comboIndex + 1) % comboMoves.length;
        }
        activeMove = null;
    }

    public void updateAttackAnimation() {
        if (attackAnimation != null && attackAnimation.isPlaying()) {
            attackAnimation.tick();
        }
    }

    public void resetAnimation() {
        this.attackBox = null;
        this.attackDamageApplied = false;
    }

    public boolean isInAnimation() {
        return attackAnimation != null && attackAnimation.isPlaying();
    }

    public boolean isSwinging() {
        return isInAnimation();
    }

    public boolean touchingOtherHitBox(Arc2D arc, Rectangle2D rect) {
        if (arc == null || rect == null) return false;

        if (arc.intersects(rect)) {
            return true;
        }

        if (arc.contains(rect.getMinX(), rect.getMinY())
                || arc.contains(rect.getMaxX(), rect.getMinY())
                || arc.contains(rect.getMinX(), rect.getMaxY())
                || arc.contains(rect.getMaxX(), rect.getMaxY())) {
            return true;
        }

        Point2D arcStart = arc.getStartPoint();
        Point2D arcEnd = arc.getEndPoint();
        if (rect.contains(arcStart) || rect.contains(arcEnd)) {
            return true;
        }

        if (arc.getArcType() == Arc2D.CHORD || arc.getArcType() == Arc2D.PIE) {
            Point2D center = new Point2D.Double(arc.getCenterX(), arc.getCenterY());
            Line2D radial1 = new Line2D.Double(center, arcStart);
            Line2D radial2 = new Line2D.Double(center, arcEnd);

            if (radial1.intersects(rect) || radial2.intersects(rect)) {
                return true;
            }

            if (arc.getArcType() == Arc2D.CHORD) {
                Line2D chord = new Line2D.Double(arcStart, arcEnd);
                if (chord.intersects(rect)) {
                    return true;
                }
            }
        }

        return false;
    }

    public int secondsToTicks(double seconds) {
        return (int) (seconds * 60);
    }

    public Arc2D getAttackBox() {
        return attackBox;
    }

    public boolean isAttackDamageApplied() {
        return attackDamageApplied;
    }

    @Override
    public void leftClickAct(int clickXDown, int clickYDown, int clickXUp, int clickYUp, int currentTick, int processedX, int processedY) {
        this.attack(clickXDown, clickYDown, currentTick, processedX, processedY);
    }
}
