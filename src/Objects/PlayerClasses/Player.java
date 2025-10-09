package Objects.PlayerClasses;

import Objects.VisualObject;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Set;
import java.awt.geom.*;
import Objects.EnemyClasses.Enemy;

public class Player extends VisualObject {
    protected int health = 100;
    protected int maxHealth = 100;
    protected int overHealAmount = 0;
    protected int speed = 1;
    protected int overHealExpireTime = -1;
    protected double rollSpeed = 1;
    public int maxRolls = 1;
    public int rolls = maxRolls;
    public int rollRechargeTick = -1;
    protected int attackAnimationLength = this.secondsToTicks(0.6);
    protected int attackDamageDelay = this.secondsToTicks(0.2);
    protected int attackAnimationTick = -1;
    protected int attackDamageTick = -1;
    protected int attackRange = 100;
    protected int attackAngle = 30;
    protected Rectangle2D hitbox;
    
    public Player(int x, int y, int maxHealth) {
        super(x, y, "images/rus.jpg");
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.hitbox = new Rectangle2D.Double();
    }

    public void attackAnimationBegin(int currentTick) {
        if (this.attackAnimationTick == -1) {
            this.attackAnimationTick = currentTick + this.attackAnimationLength;
            this.attackDamageTick = this.attackAnimationTick + this.attackDamageDelay;
            // Start attack animation
        }
        
    }

    public void attack(int clickXDown, int clickYDown, int clickXUp, int clickYUp, int currentTick) {
        if (clickXDown>-1 && clickYDown>-1) {
        	attackAnimationBegin(currentTick);
        }
    }

    public void checkAttackAnimation(int currentTick, int clickXDown, int clickYDown, int clickXUp, int clickYUp, ArrayList<Enemy> others) {
        if (this.attackDamageTick != -1 && currentTick >= this.attackDamageTick && currentTick < this.attackAnimationTick) {
            // Perform attack damage logic here
        	
            this.attackDamageTick = -1; // Reset attack damage tick
        }
        if (this.attackAnimationTick != -1 && currentTick >= this.attackAnimationTick) {
            this.resetAnimation();
        }
    }
    public void resetAnimation(){
        this.attackAnimationTick = -1;
        this.attackDamageTick = -1;
    }
    public boolean isInAnimation() {
        return this.attackAnimationTick != -1 || this.attackDamageTick != -1;
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
    public void checkAllTick(int currentTick, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, ArrayList<Enemy> others) {
        this.checkRolls(currentTick);
        this.checkAttackAnimation(currentTick, clickXDown, clickYDown, clickXUp, clickYUp, others);
        this.checkOverHeal(currentTick);
    }
    public void overHeal(int amount, int currentTick, int duration) {
        this.overHealAmount += amount;
        this.overHealExpireTime = currentTick + duration;
    }

    public void checkOverHeal(int currentTick) {
        if (this.overHealAmount > 0 && currentTick >= this.overHealExpireTime) {
            this.overHealAmount = 0;
            this.overHealExpireTime = -1;
        }
    }

    public void checkRolls(int currentTick) {
        if (this.rollRechargeTick != -1 && currentTick >= this.rollRechargeTick) {
            this.rolls += 1;
            this.rollRechargeTick = -1; // Reset recharge tick
            if (this.rolls < this.maxRolls) {
                this.checkRolls(currentTick + (int) (180 / this.rollSpeed));
            }
        }
    }

    public int getHealth() {
        return this.health;
    }

    public int getMaxHealth() {
        return this.maxHealth;
    }

    public void keyHandler(Set<Integer> pressedKeys) {
        if (pressedKeys.contains(87)) { // W key
            this.y -= this.speed;
        }
        if (pressedKeys.contains(83)) { // S key
            this.y += this.speed;
        }
        if (pressedKeys.contains(65)) { // A key
            this.x -= this.speed;
        }
        if (pressedKeys.contains(68)) { // D key
            this.x += this.speed;
        }
    }
    
    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount, ArrayList<Enemy> others) {
        this.width = 50; // Set width for collision detection
        this.height = 50; // Set height for collision detection
        this.draw(g);
        // this.keyHandler(pressedKeys);
        this.checkAllTick(tickCount, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, others);
    }

    public void rollCooldown(int currentTick, int cooldownTime) {
        if (this.rollRechargeTick == -1) {
            this.rollRechargeTick = currentTick + cooldownTime;
        }
        this.rolls -= 1;
    }
    public boolean hits(Arc2D arc, Rectangle2D rect) {
        // Step 1: Check for general intersection using Arc2D's built-in method.
        // This checks for intersections with the curved segment.
        if (arc.intersects(rect)) {
            return true;
        }

        // Step 2: Check if any of the rectangle's four corners are inside the arc.
        if (arc.contains(rect.getMinX(), rect.getMinY()) ||
            arc.contains(rect.getMaxX(), rect.getMinY()) ||
            arc.contains(rect.getMinX(), rect.getMaxY()) ||
            arc.contains(rect.getMaxX(), rect.getMaxY())) {
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

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getSpeed() {
        return this.speed;
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }
}
