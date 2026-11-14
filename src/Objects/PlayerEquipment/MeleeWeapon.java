package Objects.PlayerEquipment;

import Objects.Animations.Animation;
import Objects.Animations.AnimationSegment;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class MeleeWeapon extends Item {

    protected Animation attackAnimation;
    protected int attackAnimationLength = this.secondsToTicks(0.6);
    protected int attackDamageDelay = this.secondsToTicks(0.2);
    protected int attackRange = 100;
    protected int attackAngle = 30;
    protected Arc2D attackBox;
    protected boolean attackDamageApplied = false;

    public MeleeWeapon(int attackRange, int attackAngle) {
        this.attackRange = attackRange;
        this.attackAngle = attackAngle;
    }

    public void attack(int clickXDown, int clickYDown, int currentTick, int processedX, int processedY) {
        this.startAttackAnimation();
        double deltaX = -1 * (processedX - clickXDown);
        double deltaY = -1 * (processedY - clickYDown);

        // Use Math.atan2(y, x) to get the angle in radians
        // The y-coordinate difference goes first!
        double angleRadians = Math.atan2(deltaY, deltaX);

        // Convert the angle from radians to degrees
        double angleDegrees = Math.toDegrees(angleRadians);

        this.attackBox = new Arc2D.Double(
                processedX - attackRange, // x-coordinate of the top-left corner of the framing rectangle
                processedY - attackRange, // y-coordinate of the top-left corner of the framing rectangle
                attackRange * 2, // width of the framing rectangle (diameter)
                attackRange * 2, // height of the framing rectangle (diameter)
                -1 * (angleDegrees + (attackAngle / 2)), // starting angle in degrees
                attackAngle, // angular extent (length) in degrees
                Arc2D.PIE // closure type (PIE, CHORD, or OPEN)
        );
    }

    /**
     * Starts a new attack animation with swing and damage phases
     */
    public void startAttackAnimation() {
        // Don't start if already animating
        if (attackAnimation != null && attackAnimation.isPlaying()) {
            return;
        }

        attackDamageApplied = false;
        attackAnimation = new Animation();

        // Swing segment: visual animation phase (full animation duration)
        AnimationSegment swingSegment = new AnimationSegment(
                0,
                attackAnimationLength,
                () -> {
                    /* Animation swing start */ },
                () -> {
                    /* Swing tick - update visual state */ },
                null
        );

        // Damage segment: when damage is applied (single tick at damage delay point)
        AnimationSegment damageSegment = new AnimationSegment(
                attackDamageDelay,
                1,
                () -> applyAttackDamage(), // Fire damage on segment start
                null,
                null
        );

        attackAnimation.addSegment(swingSegment);
        attackAnimation.addSegment(damageSegment);
        attackAnimation.addEndAction(() -> onAttackAnimationEnd());

        attackAnimation.play();
    }

    /**
     * Apply damage from the attack (can be overridden by subclasses) Override
     * this method to implement specific damage logic
     */
    protected void applyAttackDamage() {
        if (!attackDamageApplied) {
            attackDamageApplied = true;
            // Damage logic to be implemented by subclasses
        }
    }

    /**
     * Called when attack animation completes
     */
    protected void onAttackAnimationEnd() {
        attackBox = null;
        resetAnimation();
    }

    /**
     * Updates the current attack animation (call this each frame)
     */
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
        // Step 1: Check for general intersection using Arc2D's built-in method.
        // This checks for intersections with the curved segment.
        if (arc.intersects(rect)) {
            return true;
        }

        // Step 2: Check if any of the rectangle's four corners are inside the arc.
        if (arc.contains(rect.getMinX(), rect.getMinY())
                || arc.contains(rect.getMaxX(), rect.getMinY())
                || arc.contains(rect.getMinX(), rect.getMaxY())
                || arc.contains(rect.getMaxX(), rect.getMaxY())) {
            return true;
        }

        // Step 3: Check if the arc's endpoints are inside the rectangle.
        Point2D arcStart = arc.getStartPoint();
        Point2D arcEnd = arc.getEndPoint();
        if (rect.contains(arcStart) || rect.contains(arcEnd)) {
            return true;
        }

        // Step 4: Handle special arc types with straight segments (CHORD and PIE).
        // This checks for intersections between the straight segments and the rectangle's edges.
        if (arc.getArcType() == Arc2D.CHORD || arc.getArcType() == Arc2D.PIE) {
            Point2D center = new Point2D.Double(arc.getCenterX(), arc.getCenterY());

            // Define the straight-line segments for the chord or pie.
            Line2D radial1 = new Line2D.Double(center, arcStart);
            Line2D radial2 = new Line2D.Double(center, arcEnd);

            // Create line segments for the rectangle's four sides.
            Line2D rectTop = new Line2D.Double(rect.getMinX(), rect.getMinY(), rect.getMaxX(), rect.getMinY());
            Line2D rectRight = new Line2D.Double(rect.getMaxX(), rect.getMinY(), rect.getMaxX(), rect.getMaxY());
            Line2D rectBottom = new Line2D.Double(rect.getMinX(), rect.getMaxY(), rect.getMaxX(), rect.getMaxY());
            Line2D rectLeft = new Line2D.Double(rect.getMinX(), rect.getMinY(), rect.getMinX(), rect.getMaxY());

            // Check for intersections between the straight arc segments and the rectangle's edges.
            if (radial1.intersects(rect) || radial2.intersects(rect)) {
                return true;
            }

            // For a CHORD arc, also check the chord line itself.
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
        return (int) (seconds * 60); // Assuming 60 ticks per second
    }

    /**
     * Get the current attack box (null if not attacking)
     */
    public Arc2D getAttackBox() {
        return attackBox;
    }

    /**
     * Check if damage has been applied in current attack
     */
    public boolean isAttackDamageApplied() {
        return attackDamageApplied;
    }

    @Override
    public void leftClickAct(int clickXDown, int clickYDown, int clickXUp, int clickYUp, int currentTick, int processedX, int processedY) {
        this.attack(clickXDown, clickYDown, currentTick, processedX, processedY);
    }
}
