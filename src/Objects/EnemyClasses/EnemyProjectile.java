package Objects.EnemyClasses;

import Objects.PlayerClasses.Player;
import Objects.ProjectileCollision;
import Objects.VisualObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

final class EnemyProjectile extends VisualObject {
    private final Player target;
    private final double vx;
    private final double vy;
    private final int damage;
    private int ttl = 150;
    private final List<? extends VisualObject> walls;

    EnemyProjectile(int x, int y, Player target, int damage, double speed,
                    List<? extends VisualObject> walls) {
        super(x, y, 12, 12, new Color(255, 120, 60));
        this.target = target;
        this.damage = damage;
        this.walls = walls;

        double dx = target.getX() - x;
        double dy = target.getY() - y;
        double distance = Math.max(1, Math.hypot(dx, dy));
        this.vx = dx / distance * speed;
        this.vy = dy / distance * speed;
    }

    @Override public void update(int tick) {
        double nextX = x + vx, nextY = y + vy;
        if (ProjectileCollision.hitsWall(x + width / 2.0, y + height / 2.0,
                nextX + width / 2.0, nextY + height / 2.0, width, height, walls)) {
            visible = false;
            return;
        }
        x = (int) Math.round(nextX);
        y = (int) Math.round(nextY);
        if (--ttl <= 0) {
            visible = false;
            return;
        }
        if (target != null && checkCollision(target)) {
            target.hurt(damage);
            visible = false;
        }
    }

    @Override
    public void render(Graphics2D g, int tick) {
        if (visible) draw(g);
    }
}
