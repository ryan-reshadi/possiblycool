package Objects.Effects;

import Levels.BaseLevel;
import Objects.EnemyClasses.Enemy;
import Objects.PlayerClasses.Player;
import Objects.VisualObject;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Telegraphed dash-slash ultimate for the Rogue: outlines path, resolves damage, teleports player. */
public class ThousandCutsEffect extends VisualObject {

    private enum Phase { TELEGRAPH, RESOLVED }

    private final Player player;
    private final BaseLevel level;
    private final int startTick;
    private final int telegraphTicks;
    private final int baseDamage;
    private final double maxHealthPercent;
    private final int pathWidth;
    private final int sx, sy;
    private final int ex, ey; // clipped end (center target)
    private final Polygon pathPolygon;
    private final List<Slash> slashes = new ArrayList<>();
    private Phase phase = Phase.TELEGRAPH;
    private boolean resolved = false;
    private static final Random RNG = new Random();

    public ThousandCutsEffect(Player player, BaseLevel level, int currentTick, int targetX, int targetY,
                              int pathLength, int pathWidth, int telegraphTicks,
                              int baseDamage, double maxHealthPercent) {
        super(0, 0, 1, 1, Color.WHITE);
        this.player = player;
        this.level = level;
        this.startTick = currentTick;
        this.telegraphTicks = telegraphTicks;
        this.baseDamage = baseDamage;
        this.maxHealthPercent = maxHealthPercent;
        this.pathWidth = pathWidth;
        this.sx = player.getX() + player.getWidth() / 2;
        this.sy = player.getY() + player.getHeight() / 2;

        double dx = targetX - sx;
        double dy = targetY - sy;
        double dist = Math.hypot(dx, dy);
        if (dist < 0.001) { dx = 1; dy = 0; dist = 1; }
        double nx = dx / dist;
        double ny = dy / dist;
        int desiredEndX = sx + (int) Math.round(nx * pathLength);
        int desiredEndY = sy + (int) Math.round(ny * pathLength);
        int[] clipped = level.clipDashPath(sx, sy, desiredEndX, desiredEndY, player.getWidth(), player.getHeight());
        this.ex = clipped[0];
        this.ey = clipped[1];
        this.pathPolygon = buildPathPolygon(sx, sy, ex, ey, pathWidth);
    }

    private static Polygon buildPathPolygon(int x1, int y1, int x2, int y2, int width) {
        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.hypot(dx, dy);
        if (len < 0.001) len = 1;
        double px = -dy / len * (width / 2.0);
        double py = dx / len * (width / 2.0);
        int[] xs = {
                (int) Math.round(x1 + px), (int) Math.round(x2 + px),
                (int) Math.round(x2 - px), (int) Math.round(x1 - px)
        };
        int[] ys = {
                (int) Math.round(y1 + py), (int) Math.round(y2 + py),
                (int) Math.round(y2 - py), (int) Math.round(y1 - py)
        };
        return new Polygon(xs, ys, 4);
    }

    @Override
    public void update(int tickCount) {
        int elapsed = tickCount - startTick;
        if (!resolved && elapsed >= telegraphTicks) {
            resolve(tickCount);
        }
        if (phase == Phase.RESOLVED && elapsed >= telegraphTicks + 18) {
            visible = false;
        }
    }

    private void resolve(int tickCount) {
        resolved = true;
        phase = Phase.RESOLVED;
        // Generate slash marks along the path
        double dx = ex - sx, dy = ey - sy;
        double len = Math.hypot(dx, dy);
        int count = Math.max(6, (int) (len / 35));
        for (int i = 0; i < count; i++) {
            double t = (i + 0.5) / count;
            double cx = sx + dx * t;
            double cy = sy + dy * t;
            double perpX = -dy / Math.max(1, len);
            double perpY = dx / Math.max(1, len);
            double offset = (RNG.nextDouble() - 0.5) * (pathWidth * 0.6);
            cx += perpX * offset;
            cy += perpY * offset;
            double angle = RNG.nextDouble() * Math.PI;
            int slashLen = 18 + RNG.nextInt(28);
            slashes.add(new Slash(cx, cy, angle, slashLen));
        }

        // Damage enemies intersecting path
        if (level != null) {
            for (Enemy enemy : level.getEnemies()) {
                if (enemy == null || enemy.isDead()) continue;
                Rectangle2D hb = enemy.getHitbox();
                if (hb == null) continue;
                Area area = new Area(pathPolygon);
                area.intersect(new Area(hb));
                if (!area.isEmpty()) {
                    int damage = baseDamage + (int) Math.round(enemy.getMaxHealth() * maxHealthPercent);
                    int before = enemy.getHealth();
                    int ecx = enemy.getX() + enemy.getWidth() / 2;
                    int ecy = enemy.getY() + enemy.getHeight() / 2;
                    enemy.hit(damage, ecx - (ex - sx), ecy - (ey - sy), 8.0, tickCount);
                    if (player != null) player.chargeUltimateForDamage(before - enemy.getHealth());
                }
            }
            // Teleport player to clipped end (center-based)
            if (player != null) {
                int targetPlayerX = ex - player.getWidth() / 2;
                int targetPlayerY = ey - player.getHeight() / 2;
                player.setX(targetPlayerX);
                player.setY(targetPlayerY);
                player.getHitbox().setRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());
            }
        }
    }

    @Override
    public void render(Graphics2D g, int tickCount) { draw(g); }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (phase == Phase.TELEGRAPH) {
            g2.setColor(new Color(255, 255, 255, 70));
            g2.fillPolygon(pathPolygon);
            g2.setColor(new Color(255, 255, 255, 220));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawPolygon(pathPolygon);
            g2.setStroke(new BasicStroke(1f));
        } else {
            g2.setColor(new Color(255, 70, 70, 60));
            g2.fillPolygon(pathPolygon);
            g2.setColor(new Color(255, 90, 90, 220));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawPolygon(pathPolygon);
            g2.setStroke(new BasicStroke(1f));
            // draw slashes
            for (Slash s : slashes) {
                s.draw(g2);
            }
        }
    }

    private static class Slash {
        final double cx, cy, angle; final int len;
        Slash(double cx, double cy, double angle, int len) { this.cx = cx; this.cy = cy; this.angle = angle; this.len = len; }
        void draw(Graphics2D g2) {
            double dx = Math.cos(angle) * len / 2.0;
            double dy = Math.sin(angle) * len / 2.0;
            g2.setColor(new Color(255, 255, 255, 230));
            g2.setStroke(new BasicStroke(3f));
            g2.drawLine((int)(cx-dx),(int)(cy-dy),(int)(cx+dx),(int)(cy+dy));
            g2.setColor(new Color(220, 40, 40, 220));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine((int)(cx-dx),(int)(cy-dy),(int)(cx+dx),(int)(cy+dy));
            g2.setStroke(new BasicStroke(1f));
        }
    }
}
