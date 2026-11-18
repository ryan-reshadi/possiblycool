package Objects.EnemyClasses;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;

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
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = this.color != null ? this.color : new Color(150, 0, 0);
            // glow
            g2.setColor(new Color(255, 120, 40, 60));
            g2.fillOval(this.x - 6, this.y - 6, this.width + 12, this.height + 12);
            g2.setColor(new Color(255, 80, 20, 40));
            g2.fillOval(this.x - 2, this.y - 2, this.width + 4, this.height + 4);
            // body
            g2.setColor(c);
            g2.fillRoundRect(this.x, this.y, this.width, this.height, 10, 10);
            // highlight
            g2.setColor(new Color(255, 160, 120, 80));
            g2.fillRoundRect(this.x + 6, this.y + 6, 10, this.height - 20, 4, 4);
            // outline
            g2.setColor(new Color(255, 200, 80));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(this.x, this.y, this.width, this.height, 10, 10);
            g2.setStroke(new BasicStroke(1f));
            // eyes
            g2.setColor(new Color(255, 230, 120));
            g2.fillRect(this.x + 14, this.y + 22, 8, 6);
            g2.fillRect(this.x + this.width - 22, this.y + 22, 8, 6);
            g2.setColor(new Color(80, 0, 0));
            g2.fillRect(this.x + 17, this.y + 24, 2, 2);
            g2.fillRect(this.x + this.width - 19, this.y + 24, 2, 2);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(new Color(255, 220, 160));
            g2.drawString("BOSS", this.x + 14, this.y + 48);
        } finally {
            g2.dispose();
        }
    }
}
