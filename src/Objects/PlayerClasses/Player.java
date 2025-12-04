package Objects.PlayerClasses;

import Objects.VisualObject;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Set;
import java.awt.geom.*;
import Objects.Entity;

public class Player extends Entity {
//    protected int health = 100;
//    protected int maxHealth = 100;
//	protected int speed = 1;
    protected int overHealAmount = 0;
    protected int overHealExpireTime = -1;
    protected double rollSpeed = 1;
    public int maxRolls = 1;
    public int rolls = maxRolls;
    public int rollRechargeTick = -1;
//    protected int attackAnimationLength = this.secondsToTicks(0.6);
//    protected int attackDamageDelay = this.secondsToTicks(0.2);
//    protected int attackAnimationTick = -1;
//    protected int attackDamageTick = -1;
//    protected int attackRange = 100;
//    protected int attackAngle = 30;
//    protected Rectangle2D hitbox;
    
    public Player(int x, int y, int maxHealth) {
        super(x, y, "images/rus.jpg");
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.hitbox = new Rectangle2D.Double();
        System.out.println("Heh goon, the hitbox is working!");
    }

    
    public void checkAllTick(int currentTick, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, ArrayList<VisualObject> others) {
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
    	this.attackAnimationBegin(currentTick);
    	
    }
    
    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount, ArrayList<VisualObject> others) {
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

    
    public void testWorking() {
    	System.out.println("Player is working!");
    }
}
