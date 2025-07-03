package Objects.EnemyClasses;

import Objects.PlayerClasses.Player;
import Objects.VisualObject;

public class Enemy extends VisualObject {

    protected int health;
    protected int damage;
    protected int aggroRange;
    protected Player targetPlayer = null;
    protected int x; // Enemy's x-coordinate
    protected int y; // Enemy's y-coordinate

    public Enemy(int x, int y, int health, int damage, int aggroRange, String imgPath) {
        super(x, y, imgPath);
        this.health = health;
        this.damage = damage;
        this.aggroRange = aggroRange;
    }

    public int getHealth() {
        return this.health;
    }

    public void damage(int dmg) {
        this.health -= dmg;
        if (this.health < 0) {
            this.health = 0;
        }
    }

    public void checkAggro(Player target) {
        if (target != null) {
            double distance = Math.sqrt(Math.pow(target.x - this.targetPlayer.x, 2) + Math.pow(target.y - this.targetPlayer.y, 2));
            if (distance <= this.aggroRange) {
                this.targetPlayer = target;
            } else {
                this.targetPlayer = null;
            }
        }
    }

    public double getDistanceToPlayer() {
        if (this.targetPlayer != null) {
            return Math.sqrt(Math.pow(this.targetPlayer.x - this.x, 2) + Math.pow(this.targetPlayer.y - this.y, 2));
        }
        return Double.MAX_VALUE; // No player targeted
    }

    // Meant to be overridden by subclasses
    public void aggroLogic() {
    }
}
