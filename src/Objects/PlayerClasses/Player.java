package Objects.PlayerClasses;

import Objects.VisualObject;
import java.awt.Graphics;
import java.util.Set;

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
    public Player(int x, int y, int maxHealth) {
        super(x, y, "images/rus.jpg");
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void attackAnimationBegin(int currentTick) {
        if (this.attackAnimationTick == -1) {
            this.attackAnimationTick = currentTick + this.attackAnimationLength;
            this.attackDamageTick = this.attackAnimationTick + this.attackDamageDelay;
            // Start attack animation
        }
        
    }

    public void attack(int clickXDown, int clickYDown, int clickXUp, int clickYUp) {
        
    }

    public void checkAttackAnimation(int currentTick, int clickXDown, int clickYDown, int clickXUp, int clickYUp) {
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
    public void checkAllTick(int currentTick, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp) {
        this.checkRolls(currentTick);
        this.checkAttackAnimation(currentTick, clickXDown, clickYDown, clickXUp, clickYUp);
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
    
    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount) {
        this.width = 50; // Set width for collision detection
        this.height = 50; // Set height for collision detection
        this.draw(g);
        // this.keyHandler(pressedKeys);
        this.checkAllTick(tickCount, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp);
    }

    public void rollCooldown(int currentTick, int cooldownTime) {
        if (this.rollRechargeTick == -1) {
            this.rollRechargeTick = currentTick + cooldownTime;
        }
        this.rolls -= 1;
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
