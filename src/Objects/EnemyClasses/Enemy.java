package Objects.EnemyClasses;

import Objects.PlayerClasses.Player;

public class Enemy {
    protected int health;
    protected int damage;
    protected int aggroRange;
    protected Player targetPlayer;
    public Enemy(int health, int damage, int aggroRange){
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
    public void checkAggro(Player target){
        if (target != null) {
            double distance = Math.sqrt(Math.pow(target.x - this.targetPlayer.x, 2) + Math.pow(target.y - this.targetPlayer.y, 2));
            if (distance <= this.aggroRange) {
                this.targetPlayer = target;
            } else {
                this.targetPlayer = null;
            }
        }
    }
}
