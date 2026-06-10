package Objects;

import Objects.PlayerEquipment.Item;
import java.awt.Color;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class Entity extends VisualObject {

    protected int health;
    protected int maxHealth;
    protected int speed = 1;

    protected int attackRange = 100;
    protected int attackAngle = 30;
    protected Rectangle2D hitbox;
    protected Item[] inventory = new Item[9];
    protected int MainHandIndex;

    public Entity(int x, int y, int width, int height, String imgPath) {
        super(x, y, width, height, imgPath);

    }

    public Entity(int x, int y, String imgPath) {
        super(x, y, imgPath);
    }

    public Entity(int x, int y, int width, int height, Color color) {
        super(x, y, width, height, color);

    }

    private void fieldSetup() {
        health = maxHealth;

    }

    public void alterStats(int healthChange, int speedChange, int attackRangeChange, int attackAngleChange) {
        this.health += healthChange;
        this.speed += speedChange;
        this.attackRange += attackRangeChange;
        this.attackAngle += attackAngleChange;
    }

    public void changeStats(int healthChange, int speedChange, int attackRangeChange, int attackAngleChange) {
        this.health = healthChange;
        this.speed = speedChange;
        this.attackRange = attackRangeChange;
        this.attackAngle = attackAngleChange;
    }

    public void hurt(int damage) {
        this.health -= damage;
    }

    public void heal(int amount) {
        this.health += amount;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
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

    public double getSpeed() {
        return this.speed;
    }

    // Getters
    public int getHealth() {
        return this.health;
    }

    public int getMaxHealth() {
        return this.maxHealth;
    }

    public int getAttackRange() {
        return this.attackRange;
    }

    public int getAttackAngle() {
        return this.attackAngle;
    }

    public Rectangle2D getHitbox() {
        return this.hitbox;
    }

    public Item[] getInventory() {
        return this.inventory;
    }

    public int getMainHandIndex() {
        return this.MainHandIndex;
    }

    // Setters
    public void setHealth(int health) {
        this.health = health;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setAttackRange(int attackRange) {
        this.attackRange = attackRange;
    }

    public void setAttackAngle(int attackAngle) {
        this.attackAngle = attackAngle;
    }

    public void setHitbox(Rectangle2D hitbox) {
        this.hitbox = hitbox;
    }

    public void setInventory(Item[] inventory) {
        this.inventory = inventory;
    }

    public void setMainHandIndex(int mainHandIndex) {
        this.MainHandIndex = mainHandIndex;
    }

}
