package Objects.EnemyClasses;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class MeleeEnemy extends Enemy {

    protected int attackCooldown = 45; // 45 ticks between attacks (~0.75s)
    protected int lastAttackTick = -1;

    public MeleeEnemy(int x, int y, int health, int damage, int aggroRange, int speed, int attackRange) {
        super(x, y, 40, 40, health, damage, aggroRange, new Color(200, 50, 50));
        this.speed = speed;
        this.attackRange = attackRange;
    }

    public MeleeEnemy(int x, int y) {
        this(x, y, 60, 12, 350, 2, 40);
    }

    @Override
    public void aggroLogic(int currentTick) {
        if (weapon != null) { weapon.update(this, targetPlayer, currentTick); return; }
        if (this.targetPlayer != null && !this.targetPlayer.isDead()) {
            double distance = getDistanceToPlayer();
            if (distance > this.attackRange) {
                moveTowardsPlayer(distance, currentTick);
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
        if (weapon instanceof TankWeapon) {
            ((TankWeapon) weapon).cancelAttack();
        }
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
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            if (this.image != null) {
                super.draw(g2);
            } else {
                Color c = this.color != null ? this.color : Color.RED;
                // shadow
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillOval(this.x + 2, this.y + this.height - 8, this.width - 4, 8);
                // body
                g2.setColor(c);
                g2.fillRoundRect(this.x + 2, this.y + 2, this.width - 4, this.height - 6, 6, 6);
                // highlight
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillRoundRect(this.x + 4, this.y + 4, 4, this.height - 12, 2, 2);
                // outline
                g2.setColor(new Color(40, 10, 10));
                g2.drawRoundRect(this.x + 2, this.y + 2, this.width - 4, this.height - 6, 6, 6);
                // eyes
                g2.setColor(new Color(255, 220, 120));
                g2.fillRect(this.x + 8, this.y + 12, 4, 4);
                g2.fillRect(this.x + this.width - 12, this.y + 12, 4, 4);
            }
        } finally {
            g2.dispose();
        }
    }
}
