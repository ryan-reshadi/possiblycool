package Objects;

import java.awt.Color;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public abstract class Entity extends VisualObject {
	protected int health;
    protected int maxHealth;
    protected int speed = 1;
//    protected int overHealAmount = 0;
    protected int attackAnimationLength = this.secondsToTicks(0.6);
    protected int attackDamageDelay = this.secondsToTicks(0.2);
    protected int attackAnimationTick = -1;
    protected int attackDamageTick = -1;
    protected int attackRange = 100;
    protected int attackAngle = 30;
    protected Rectangle2D hitbox;
	public Entity (int x, int y, int width, int height, String imgPath) {
		super (x, y, width, height, imgPath);
		
	}
	public Entity (int x, int y, String imgPath) {
		super (x, y, imgPath);
	}
	public Entity(int x, int y, int width, int height, Color color) {
		super (x, y, width, height, color);
		
	}
	private void fieldSetup() {
		health = maxHealth;
		
	}
	protected void attackAnimationBegin(int currentTick) {
        if (this.attackAnimationTick == -1) {
            this.attackAnimationTick = currentTick + this.attackAnimationLength;
            this.attackDamageTick = this.attackAnimationTick + this.attackDamageDelay;
            // Start attack animation
        }
        
    }

    

    public void checkAttackAnimation(int currentTick, int clickXDown, int clickYDown, int clickXUp, int clickYUp, ArrayList<VisualObject> others) {
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
    public boolean touchingOtherHitBox(Arc2D arc, Rectangle2D rect) {
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
