package Objects.PlayerClasses;

import Objects.Entity;
import Objects.PlayerEquipment.MeleeWeapon;
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

    /**
     * Get the currently equipped melee weapon (if any)
     */
    private MeleeWeapon getEquippedWeapon() {
        // System.out.println(inventory[MainHandIndex]);
        if (MainHandIndex >= 0 && MainHandIndex < inventory.length && inventory[MainHandIndex] instanceof MeleeWeapon) {
            
            return (MeleeWeapon) inventory[MainHandIndex];
        }
        return null;
    }
    
    public void checkAllTick(int currentTick, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, ArrayList<VisualObject> others) {
    	this.checkRolls(currentTick);
        
        // Update weapon animation if equipped
        MeleeWeapon weapon = getEquippedWeapon();
        if (weapon != null) {
            weapon.updateAttackAnimation();
        }
        
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
        
        MeleeWeapon weapon = getEquippedWeapon();
        if (weapon != null && weapon.getAttackBox() != null && weapon.isInAnimation()) {
            // Highlight the attack box during animation
            if (weapon.isAttackDamageApplied()) {
                g2d.setColor(Color.RED);    	
            } else {
                g2d.setColor(Color.white);
            }
            g2d.fill(weapon.getAttackBox()); 
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
        MeleeWeapon weapon = getEquippedWeapon();
        if (weapon != null) {
            System.out.println("hi");
            weapon.attack(clickXDown, clickYDown, currentTick, this.x + this.width/2, this.y + this.height/2);
        }
    }
    
    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount, ArrayList<VisualObject> others) {
        this.width = 50; // Set width for collision detection
        this.height = 50; // Set height for collision detection
        // this.keyHandler(pressedKeys);
        this.checkAllTick(tickCount, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, others);
        this.checkAttackEffects(g, tickCount);
    	this.draw(g);
    }

    public void rollCooldown(int currentTick, int cooldownTime) {
        if (this.rollRechargeTick == -1) {
            this.rollRechargeTick = currentTick + cooldownTime;
        }
        this.rolls -= 1;
    }
}
