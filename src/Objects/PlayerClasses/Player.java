package Objects.PlayerClasses;

import Objects.VisualObject;
import java.awt.Graphics;
import java.util.Set;

public class Player extends VisualObject{
    protected int health;
    protected int maxHealth;
    protected int overHealAmount = 0;
    protected int speed = 1;
    protected int overHealExpireTime = -1;
    protected double rollSpeed = 1;
    public int maxRolls = 1;
    public int rolls = maxRolls;
    public int rollRechargeTick = -1;
    public Player(int x, int y, int maxHealth) {
        super (x, y, "images/rus.jpg");
        this.health = maxHealth;
    }
    public void attack(){}
    
    public void hurt(int damage){
        this.health -= damage;
    }

    public void heal(int amount){
        this.health += amount;
        if (this.health > this.maxHealth){
            this.health = this.maxHealth;
        }
    }

    public void overHeal(int amount, int currentTick, int duration){
        this.overHealAmount += amount;
        this.overHealExpireTime = currentTick + duration;
    }
    
    public void checkOverHeal(int currentTick){
        if (this.overHealAmount>0 && currentTick >= this.overHealExpireTime){
            this.overHealAmount = 0;
            this.overHealExpireTime = -1;
        }
    }

    public void checkRolls(int currentTick){
        if (this.rollRechargeTick != -1 && currentTick >= this.rollRechargeTick) {
            this.rolls += this.maxRolls;
            this.rollRechargeTick = -1; // Reset recharge tick
            if (this.rolls< this.maxRolls){
                this.checkRolls(currentTick + (int) (180/this.rollSpeed));
            }
        }
    }
    
    public int getHealth() {
        return this.health;
    }

    public void keyHandler(Set<Integer> pressedKeys){
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
    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount){
        this.width = 50; // Set width for collision detection
        this.height = 50; // Set height for collision detection
        this.draw(g);
        // this.keyHandler(pressedKeys);
        this.checkOverHeal(tickCount);
    }
    public void rollCooldown(int cooldown, int currentTick, int cooldownTime) {
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
