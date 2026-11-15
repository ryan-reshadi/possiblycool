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

    // Armor damage reduction (0.0 = no reduction, 1.0 = full immunity)
    // Subclasses sum armorSlot reductions into this field each tick or on equip
    protected double totalArmorReduction = 0.0;

    public Entity(int x, int y, int width, int height, String imgPath) {
        super(x, y, width, height, imgPath);
    }

    public Entity(int x, int y, String imgPath) {
        super(x, y, imgPath);
    }

    public Entity(int x, int y, int width, int height, Color color) {
        super(x, y, width, height, color);
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

    /**
     * Apply damage after armor reduction. Minimum 1 damage always gets through.
     */
    public void hurt(int damage) {
        if (damage <= 0) return;
        int effective = (int) Math.max(1, damage * (1.0 - totalArmorReduction));
        this.health -= effective;
        if (this.health < 0) this.health = 0;
    }

    /**
     * Apply damage ignoring armor (true damage).
     */
    public void hurtTrue(int damage) {
        this.health -= damage;
        if (this.health < 0) this.health = 0;
    }

    public void heal(int amount) {
        this.health += amount;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }

    public boolean isDead() {
        return this.health <= 0;
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

    public double getSpeed() { return this.speed; }
    public int getHealth() { return this.health; }
    public int getMaxHealth() { return this.maxHealth; }
    public int getAttackRange() { return this.attackRange; }
    public int getAttackAngle() { return this.attackAngle; }
    public Rectangle2D getHitbox() { return this.hitbox; }
    public Item[] getInventory() { return this.inventory; }
    public int getMainHandIndex() { return this.MainHandIndex; }
    public double getTotalArmorReduction() { return this.totalArmorReduction; }

    public void setHealth(int health) { this.health = health; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    public void setSpeed(int speed) { this.speed = speed; }
    public void setAttackRange(int attackRange) { this.attackRange = attackRange; }
    public void setAttackAngle(int attackAngle) { this.attackAngle = attackAngle; }
    public void setHitbox(Rectangle2D hitbox) { this.hitbox = hitbox; }
    public void setInventory(Item[] inventory) { this.inventory = inventory; }
    public void setMainHandIndex(int mainHandIndex) {
        this.MainHandIndex = Math.max(0, Math.min(inventory.length - 1, mainHandIndex));
    }
    public void setTotalArmorReduction(double reduction) {
        this.totalArmorReduction = Math.max(0.0, Math.min(0.75, reduction)); // hard cap at 75%
    }
}
