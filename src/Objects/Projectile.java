package Objects;

import Objects.EnemyClasses.Enemy;
import Objects.PlayerEquipment.Spells.PassiveSpell;
import Objects.PlayerClasses.Player;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

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
    private List<? extends VisualObject> walls;
    private PassiveSpell[] passives;
    private Player owner;

    private int ticksAlive = 0;
    public static final int MAX_LIFETIME_TICKS = 180;

    private boolean removed = false;

    public Projectile(int x, int y, double angle, double speed, int damage, Color color, Runnable onRemove) {
        super(x, y, 10, 10, color);
        this.damage = damage;
        this.color = color;
        this.onRemove = onRemove;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
    }

    public void setEnemies(List<Enemy> enemies) {
        this.enemies = enemies;
    }

    public void setWalls(List<? extends VisualObject> walls) {
        this.walls = walls;
    }

    public void setPassives(PassiveSpell[] passives) {
        this.passives = passives;
    }

    public void setOwner(Player owner) { this.owner = owner; }

    @Override
    public void update(int tickCount) {
        if (removed) return;

        double nextX = this.x + vx;
        double nextY = this.y + vy;
        if (ProjectileCollision.hitsWall(x + width / 2.0, y + height / 2.0,
                nextX + width / 2.0, nextY + height / 2.0, width, height, walls)) {
            remove();
            return;
        }
        this.x = (int) Math.round(nextX);
        this.y = (int) Math.round(nextY);

        ticksAlive++;

        if (ticksAlive > MAX_LIFETIME_TICKS) {
            remove();
            return;
        }

        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy != null && !enemy.isDead() && checkCollision(enemy)) {
                    int previousHealth = enemy.getHealth();
                    enemy.hit(damage, x + width / 2, y + height / 2, 4.0, tickCount);
                    if (owner != null) owner.chargeUltimateForDamage(previousHealth - enemy.getHealth());
                    if (passives != null) {
                        for (PassiveSpell passive : passives) {
                            if (passive != null) {
                                passive.applyOnHit(enemy, tickCount);
                            }
                        }
                    }
                    remove();
                    return;
                }
            }
        }

    }

    private void remove() {
        if (removed) return;
        removed = true;
        if (onRemove != null) onRemove.run();
    }

    @Override
    public void render(Graphics2D g, int tickCount) {
        if (!removed) draw(g);
    }

    @Override
    public void draw(Graphics g) {
        if (removed) return;
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = color != null ? color : Color.YELLOW;
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
            g2.fillOval(x - 3, y - 3, width + 6, height + 6);
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 120));
            g2.fillOval(x - 1, y - 1, width + 2, height + 2);
            g2.setColor(c);
            g2.fillOval(x, y, width, height);
            g2.setColor(new Color(255, 255, 255, 180));
            g2.fillOval(x + 2, y + 2, Math.max(2, width / 3), Math.max(2, height / 3));
        } finally {
            g2.dispose();
        }
    }
}
