package Objects.EnemyClasses;

public class MeleeEnemy extends Enemy {

    protected int speed;
    protected int attackRange;

    public MeleeEnemy(int x, int y, int health, int damage, int aggroRange, int speed, int attackRange) {
        super(x, y, health, damage, aggroRange, "images/baseEnemy.png");
        this.speed = speed;
        this.attackRange = attackRange;
    }

    @Override
    public void aggroLogic() {
        if (this.targetPlayer != null) {
            double distance = getDistanceToPlayer();
            if (distance > this.attackRange) {
                // If the player is outside the attack range, move towards them
                moveTowardsPlayer(distance);
            } else {
                // If within attack range, attack the player
                this.targetPlayer.hurt(this.damage);
            }
            
        }
    }

    public void moveTowardsPlayer(double distance) {
        double ratio = this.speed / distance;
        this.x += (int) ((this.targetPlayer.getX() - this.x) * ratio);
        this.y += (int) ((this.targetPlayer.getY() - this.y) * ratio);
    }
}
