package Objects.Effects;

import Objects.EnemyClasses.Enemy;
import Objects.PlayerClasses.Player;
import Objects.Terrain.Border;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

/** Projectile-only barrier field; actors can cross it and are affected over time. */
public final class RallyBarricade extends Border {
    private final Player player;
    private final List<Enemy> enemies;
    private final int expiresAt;
    private int lastEffectTick;

    public RallyBarricade(int centerX, int centerY, Player player, List<Enemy> enemies, int currentTick) {
        super(centerX - 120, centerY - 9, 240, 18, new Color(75, 170, 120));
        this.player = player;
        this.enemies = enemies == null ? List.of() : enemies;
        this.expiresAt = currentTick + 480;
        this.lastEffectTick = currentTick;
    }

    @Override public void update(int tickCount) {
        if (tickCount >= expiresAt) { visible = false; return; }
        if (tickCount - lastEffectTick < 60) return;
        lastEffectTick = tickCount;
        if (player != null && Math.hypot(player.getX() + player.getWidth()/2.0 - (x + width/2.0),
                player.getY() + player.getHeight()/2.0 - (y + height/2.0)) <= 140) player.heal(2);
        for (Enemy enemy : enemies) {
            if (enemy != null && !enemy.isDead() && enemy.getX() < x + width
                    && enemy.getX() + enemy.getWidth() > x
                    && enemy.getY() < y + height + 32 && enemy.getY() + enemy.getHeight() > y - 32) {
                enemy.hurtTrue(15);
            }
        }
    }

    @Override public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(65, 190, 125, 100)); g2.fillRoundRect(x, y - 22, width, height + 44, 10, 10);
        g2.setColor(new Color(120, 255, 180, 230)); g2.fillRoundRect(x, y, width, height, 8, 8);
        g2.setColor(Color.WHITE); g2.drawRoundRect(x, y, width, height, 8, 8);
    }
}
