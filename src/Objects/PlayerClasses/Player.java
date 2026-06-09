package Objects.PlayerClasses;

import Objects.Entity;
import Objects.VisualObject;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Set;

public class Player extends Entity {
    protected int overHealAmount = 0;
    protected int overHealExpireTime = -1;
    protected double rollSpeed = 1;
    public int maxRolls = 1;
    public int rolls = maxRolls;
    public int rollRechargeTick = -1;
    
    
    public Player(int x, int y, int maxHealth) {
        super(x, y, "images/rus.jpg");
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.hitbox = new Rectangle2D.Double();
        System.out.println("Heh goon, the hitbox is working!");
    }

    
    public void checkAllTick(int currentTick, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, ArrayList<VisualObject> others) {
    	this.checkRolls(currentTick);
        this.updateAttackAnimation();
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
    public void checkAttackEffects(Graphics g, int currentTick) {
    	Graphics2D g2d = (Graphics2D) g;

    	if(attackBox!=null && this.isInAnimation()) {
    		// Highlight the attack box during animation
    		if (this.attackDamageApplied) {
    			g2d.setColor(Color.RED);    	
    		}
    		else {
    			g2d.setColor(Color.white);
    		}
    		g2d.fill(attackBox); 
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
        	System.out.println(this.y);
        	System.out.println(this.speed);
            this.y -= this.speed;
            System.out.println(this.y);
            
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
    
    public void attack(int clickXDown, int clickYDown,int currentTick) {
    	this.startAttackAnimation();
    	double deltaX =-1*(this.x+this.width/2 - clickXDown);
        double deltaY =-1 *(this.y+this.height/2 - clickYDown);

        // Use Math.atan2(y, x) to get the angle in radians
        // The y-coordinate difference goes first!
        double angleRadians = Math.atan2(deltaY, deltaX);

        // Convert the angle from radians to degrees
        double angleDegrees = Math.toDegrees(angleRadians);

    	this.attackBox = new Arc2D.Double(
    		    this.x+this.width/2 - attackRange, // x-coordinate of the top-left corner of the framing rectangle
    		    this.y+this.height/2 - attackRange, // y-coordinate of the top-left corner of the framing rectangle
    		    attackRange * 2,       // width of the framing rectangle (diameter)
    		    attackRange * 2,       // height of the framing rectangle (diameter)
    		    -1*(angleDegrees+(attackAngle/2)),            // starting angle in degrees
    		    attackAngle,           // angular extent (length) in degrees
    		    Arc2D.PIE              // closure type (PIE, CHORD, or OPEN)
    		);;
    }
    
    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount, ArrayList<VisualObject> others) {
    	this.draw(g);
        this.width = 50; // Set width for collision detection
        this.height = 50; // Set height for collision detection
        // this.keyHandler(pressedKeys);
        this.checkAllTick(tickCount, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, others);
        this.checkAttackEffects(g,tickCount);
    }

    public void rollCooldown(int currentTick, int cooldownTime) {
        if (this.rollRechargeTick == -1) {
            this.rollRechargeTick = currentTick + cooldownTime;
        }
        this.rolls -= 1;
    }

    
    public void testWorking() {
    	System.out.println("Player is working!");
    }
}
