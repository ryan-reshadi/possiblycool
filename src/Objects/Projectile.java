package Objects;

import Objects.EnemyClasses.Enemy;
import Objects.PlayerEquipment.Spells.PassiveSpell;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.Set;

/**
 * A moving projectile fired by a Wand or ranged weapon.
 * Moves each tick, checks collision against enemies, and removes itself via a Runnable callback.
 */
public class Projectile extends VisualObject {

    private double vx;
    private double vy;
    private final int damage;
    private final Color color;
    private final Runnable onRemove;

    private List<Enemy> enemies;
    private PassiveSpell[] passives;

    private int ticksAlive = 0;
    private final int maxTicks = 180; // 3 seconds at 60 ticks/sec

    private double px;
    private double py;

    private boolean removed = false;

    public Projectile(int x, int y, double angle, double speed, int damage, Color color, Runnable onRemove) {
        super(x, y, 10, 10, color);
        this.damage = damage;
        this.color = color;
        this.onRemove = onRemove;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.px = x;
        this.py = y;
    }

    public void setEnemies(List<Enemy> enemies) {
        this.enemies = enemies;
    }

    public void setPassives(PassiveSpell[] passives) {
        this.passives = passives;
    }

    @Override
    public void tick(Graphics g, Set<Integer> pressedKeys,
                     int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount) {
        if (removed) return;

        px += vx;
        py += vy;
        this.x = (int) px;
        this.y = (int) py;

        ticksAlive++;

        if (ticksAlive > maxTicks) {
            removed = true;
            if (onRemove != null) onRemove.run();
            return;
        }

        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy != null && !enemy.isDead() && checkCollision(enemy)) {
                    enemy.hurt(damage);
                    if (passives != null) {
                        for (PassiveSpell passive : passives) {
                            if (passive != null) {
                                passive.applyOnHit(enemy, tickCount);
                            }
                        }
                    }
                    removed = true;
                    if (onRemove != null) onRemove.run();
                    return;
                }
            }
        }

        draw(g);
    }

    @Override
    public void draw(Graphics g) {
        if (removed) return;
        g.setColor(color != null ? color : Color.YELLOW);
        g.fillOval(x, y, width, height);
    }
}
