package Objects.Effects;

import Objects.EnemyClasses.Enemy;
import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** A visible, target-seeking chain arrow that hits up to five distinct enemies. */
public final class TricksterArrowEffect extends VisualObject {
    private final int aimX, aimY;
    private final List<Enemy> enemies;
    private final Player owner;
    private final List<Point> path = new ArrayList<>();
    private final List<Enemy> hit = new ArrayList<>();
    private int lastX, lastY, ticks;

    public TricksterArrowEffect(int startX, int startY, int aimX, int aimY,
                                List<Enemy> enemies, Player owner) {
        super(startX, startY, 14, 14, new Color(255, 215, 65));
        this.lastX = startX; this.lastY = startY; this.aimX = aimX; this.aimY = aimY;
        this.enemies = enemies == null ? List.of() : new ArrayList<>(enemies);
        this.owner = owner;
        path.add(new Point(startX, startY));
    }

    @Override public void update(int tickCount) {
        ticks++;
        if (ticks % 6 != 0) return;
        if (hit.size() >= 5) { visible = false; return; }
        Enemy next = enemies.stream().filter(e -> e != null && !e.isDead() && !hit.contains(e))
                .filter(e -> Math.hypot(e.getX() + e.getWidth()/2.0 - lastX,
                        e.getY() + e.getHeight()/2.0 - lastY) <= (hit.isEmpty() ? 350 : 250))
                .min(Comparator.comparingDouble(e -> Math.hypot(
                        e.getX() + e.getWidth()/2.0 - (hit.isEmpty() ? aimX : lastX),
                        e.getY() + e.getHeight()/2.0 - (hit.isEmpty() ? aimY : lastY))))
                .orElse(null);
        if (next == null) { visible = false; return; }
        int x = next.getX() + next.getWidth()/2, y = next.getY() + next.getHeight()/2;
        int damage = 20 + hit.size() * 10;
        int before = next.getHealth();
        next.hit(damage, lastX, lastY, 8.0, tickCount);
        if (owner != null) owner.chargeUltimateForDamage(before - next.getHealth());
        hit.add(next); lastX = x; lastY = y; x = lastX - width / 2; y = lastY - height / 2;
        setX(x); setY(y); path.add(new Point(lastX, lastY));
        if (hit.size() >= 5 || enemies.stream().noneMatch(e -> e != null && !e.isDead() && !hit.contains(e))) visible = false;
    }

    @Override public void render(Graphics2D g, int tickCount) { draw(g); }

    @Override public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(255, 220, 90, 190));
        for (int i = 1; i < path.size(); i++) {
            Point a = path.get(i - 1), b = path.get(i);
            g2.drawLine(a.x, a.y, b.x, b.y);
        }
        g2.setColor(Color.WHITE); g2.fillOval(x, y, width, height);
        g2.setColor(new Color(255, 175, 35)); g2.drawOval(x, y, width, height);
    }
}
