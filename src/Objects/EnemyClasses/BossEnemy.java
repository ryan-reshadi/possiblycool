package Objects.EnemyClasses;

import java.awt.Color;
import java.awt.Graphics;

public class BossEnemy extends Enemy {

    protected int attackCooldown = 60; // 1 second
    protected int lastAttackTick = -1;

    public BossEnemy(int x, int y) {
        super(x, y, 70, 70, 300, 25, 450, new Color(150, 0, 0));
        this.speed = 1;
        this.width = 70;
        this.height = 70;
    }

    @Override
    public void aggroLogic(int currentTick) {
        if (weapon != null) { weapon.update(this, targetPlayer, currentTick); return; }
        if (this.targetPlayer != null && !this.targetPlayer.isDead()) {
            double distance = getDistanceToPlayer();
            if (distance > 60) {
                double spd = getCurrentSpeed(currentTick);
                double ratio = spd / distance;
                moveWithWallCollision(
                        (int) Math.round((this.targetPlayer.getX() - this.x) * ratio),
                        (int) Math.round((this.targetPlayer.getY() - this.y) * ratio));
            } else if (hasLineOfSightTo(this.targetPlayer)) {
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

    @Override
    public void draw(Graphics g) {
        g.setColor(this.color != null ? this.color : Color.RED);
        g.fillRect(this.x, this.y, this.width, this.height);
        g.setColor(Color.ORANGE);
        g.drawRect(this.x, this.y, this.width, this.height);
        g.drawString("BOSS", this.x + 18, this.y + 40);
    }
}
