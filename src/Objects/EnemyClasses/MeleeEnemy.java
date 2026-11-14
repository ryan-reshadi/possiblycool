package Objects.EnemyClasses;

import java.awt.Color;
import java.awt.Graphics;

public class MeleeEnemy extends Enemy {

    protected int attackCooldown = 45; // 45 ticks between attacks (~0.75s)
    protected int lastAttackTick = -1;

    public MeleeEnemy(int x, int y, int health, int damage, int aggroRange, int speed, int attackRange) {
        super(x, y, health, damage, aggroRange, "images/baseEnemy.png");
        this.speed = speed;
        this.attackRange = attackRange;
        this.color = new Color(200, 50, 50);
    }

    public MeleeEnemy(int x, int y) {
        this(x, y, 60, 12, 350, 2, 40);
    }

    @Override
    public void aggroLogic(int currentTick) {
        if (this.targetPlayer != null && !this.targetPlayer.isDead()) {
            double distance = getDistanceToPlayer();
            if (distance > this.attackRange) {
                moveTowardsPlayer(distance, currentTick);
            } else {
                if (lastAttackTick == -1 || currentTick >= lastAttackTick + attackCooldown) {
                    this.targetPlayer.hurt(this.damage);
                    lastAttackTick = currentTick;
                }
            }
        }
    }

    @Override
    protected void lastAttackInterrupted() {
        lastAttackTick = Math.max(lastAttackTick, interruptedUntilTick);
    }

    @Override
    protected int getAttackCooldownTicks() {
        return attackCooldown;
    }

    public void moveTowardsPlayer(double distance, int currentTick) {
        if (distance <= 0) return;
        double spd = getCurrentSpeed(currentTick);
        double ratio = spd / distance;
        moveWithWallCollision(
                (int) Math.round((this.targetPlayer.getX() - this.x) * ratio),
                (int) Math.round((this.targetPlayer.getY() - this.y) * ratio));
    }

    @Override
    public void draw(Graphics g) {
        if (this.image != null) {
            super.draw(g);
        } else {
            g.setColor(this.color != null ? this.color : Color.RED);
            g.fillRect(this.x, this.y, this.width, this.height);
            g.setColor(Color.BLACK);
            g.drawRect(this.x, this.y, this.width, this.height);
        }
    }
}
