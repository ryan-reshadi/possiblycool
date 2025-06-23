package Objects.PlayerClasses;

import Objects.VisualObject;
import java.awt.Graphics;
import java.util.Set;

public class Player extends VisualObject{
    protected int health;
    protected int maxHealth;
    protected int overHealAmount = 0;
    protected int speed = 5;
    protected int overHealExpireTime = -1;
    protected double rollSpeed = 1;
    public Player(int x, int y, int maxHealth) {
        super (x, y, "src/Images/player-tank.jpg");
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

    public int getHealth() {
        return this.health;
    }

    @Override
    public void specTickFunc(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount) {

    }
}
