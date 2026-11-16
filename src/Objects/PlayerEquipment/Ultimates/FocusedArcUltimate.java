package Objects.PlayerEquipment.Ultimates;

import GameRun.InputFrame;
import Levels.BaseLevel;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Ultimate;
import Objects.VisualObject;
import Objects.EnemyClasses.Enemy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FocusedArcUltimate extends Ultimate {

    public static final int DURATION_TICKS = 360; // 6 seconds
    public static final int BEAM_RANGE = 420;
    public static final int BEAM_DAMAGE = 6; // per pulse
    public static final int CHAIN_DAMAGE = 3;
    public static final int PULSE_INTERVAL = 6; // 10 pulses/s
    public static final int CHAIN_RADIUS = 180;
    public static final int MAX_CHAINS = 3;
    public static final int STUN_TICKS = 18;
    public static final int CHAIN_STUN_TICKS = 12;

    private final ArcBeamVisual beamVisual = new ArcBeamVisual();
    private int lastPulseTick = -999;

    @Override
    public String getId() { return "focused-arc"; }

    @Override
    public String getName() { return "Focused Arc"; }

    @Override
    public String getDescription() {
        return "Hold left click to fire a chaining lightning beam. Beam deals more damage than its chains; stuns targets.";
    }

    @Override
    public Color getIconColor() { return new Color(120, 210, 255); }

    @Override
    protected void activate(Player player, BaseLevel level, int currentTick, int targetX, int targetY) {
        active = true;
        activeMaxTicks = DURATION_TICKS;
        activeRemainingTicks = DURATION_TICKS;
        lastPulseTick = currentTick - PULSE_INTERVAL;
        if (!beamVisual.added) {
            level.queueAddVisualObject(beamVisual);
            beamVisual.added = true;
        }
        beamVisual.setBeamVisible(false);
    }

    @Override
    public boolean tryRetrigger(Player player, BaseLevel level, int currentTick, int targetX, int targetY) {
        // No early cancel specified for mage; ignore retrigger while active.
        return active;
    }

    @Override
    protected void onEnd(Player player, BaseLevel level, int currentTick, boolean cancelled) {
        activeMaxTicks = 0;
        activeRemainingTicks = 0;
        beamVisual.setBeamVisible(false);
        beamVisual.clear();
    }

    @Override
    public boolean overridesWeaponAttack() {
        return active;
    }

    @Override
    public void handleHeldAttack(Player player, BaseLevel level, int currentTick, InputFrame input) {
        if (!active || input == null || !input.isMouseHeld()) {
            beamVisual.setBeamVisible(false);
            beamVisual.clear();
            return;
        }
        int sx = player.getX() + player.getWidth() / 2;
        int sy = player.getY() + player.getHeight() / 2;
        int aimWorldX = input.getMouseX() + level.getCameraX();
        int aimWorldY = input.getMouseY() + level.getCameraY();
        double dx = aimWorldX - sx;
        double dy = aimWorldY - sy;
        double dist = Math.hypot(dx, dy);
        if (dist < 0.001) { dx = 1; dy = 0; dist = 1; }
        int ex = sx + (int) Math.round(dx / dist * BEAM_RANGE);
        int ey = sy + (int) Math.round(dy / dist * BEAM_RANGE);

        List<Enemy> enemies = level.getEnemies();
        BeamHit primary = findBeamHit(sx, sy, ex, ey, enemies, null);
        int beamEndX = primary == null ? ex : primary.hitX;
        int beamEndY = primary == null ? ey : primary.hitY;

        List<Segment> segments = new ArrayList<>();
        segments.add(new Segment(sx, sy, beamEndX, beamEndY, true));
        List<Enemy> damaged = new ArrayList<>();
        if (primary != null) damaged.add(primary.enemy);

        // Chains from primary (or beam end area) to nearby enemies
        Set<Enemy> chained = new HashSet<>();
        if (primary != null) chained.add(primary.enemy);
        int chainOriginX = beamEndX;
        int chainOriginY = beamEndY;
        Enemy chainFrom = primary == null ? null : primary.enemy;
        for (int i = 0; i < MAX_CHAINS; i++) {
            int ox = chainOriginX;
            int oy = chainOriginY;
            Enemy next = enemies.stream()
                    .filter(e -> e != null && !e.isDead() && !chained.contains(e))
                    .filter(e -> distToEnemy(ox, oy, e) <= CHAIN_RADIUS)
                    .min(Comparator.comparingDouble(e -> distToEnemy(ox, oy, e)))
                    .orElse(null);
            if (next == null) break;
            int nx = next.getX() + next.getWidth() / 2;
            int ny = next.getY() + next.getHeight() / 2;
            segments.add(new Segment(ox, oy, nx, ny, false));
            chained.add(next);
            damaged.add(next);
            chainOriginX = nx;
            chainOriginY = ny;
            chainFrom = next;
        }

        beamVisual.setSegments(segments);
        beamVisual.setBeamVisible(true);

        if (currentTick - lastPulseTick >= PULSE_INTERVAL) {
            lastPulseTick = currentTick;
            // apply damage/stun once per pulse
            Set<Enemy> unique = new HashSet<>(damaged);
            for (Enemy enemy : unique) {
                if (enemy == null || enemy.isDead()) continue;
                boolean isPrimary = primary != null && enemy == primary.enemy;
                int dmg = isPrimary ? BEAM_DAMAGE : CHAIN_DAMAGE;
                int stun = isPrimary ? STUN_TICKS : CHAIN_STUN_TICKS;
                int before = enemy.getHealth();
                int srcX = isPrimary ? sx : enemy.getX() + enemy.getWidth() / 2;
                int srcY = isPrimary ? sy : enemy.getY() + enemy.getHeight() / 2;
                enemy.hit(dmg, srcX, srcY, 1.0, currentTick);
                enemy.stun(stun, currentTick);
                player.chargeUltimateForDamage(0); // blocked while active, but safe
                if (before != enemy.getHealth()) {
                    // damage dealt tracking could go here if needed
                }
            }
        }
    }

    private static double distToEnemy(int x, int y, Enemy e) {
        int ex = e.getX() + e.getWidth() / 2;
        int ey = e.getY() + e.getHeight() / 2;
        return Math.hypot(ex - x, ey - y);
    }

    private static BeamHit findBeamHit(int x1, int y1, int x2, int y2, List<Enemy> enemies, Enemy ignore) {
        Line2D beam = new Line2D.Double(x1, y1, x2, y2);
        BeamHit best = null;
        double bestDist = Double.MAX_VALUE;
        if (enemies == null) return null;
        for (Enemy e : enemies) {
            if (e == null || e.isDead() || e == ignore) continue;
            Rectangle2D r = new Rectangle2D.Double(e.getX(), e.getY(), e.getWidth(), e.getHeight());
            if (beam.intersects(r)) {
                int cx = e.getX() + e.getWidth() / 2;
                int cy = e.getY() + e.getHeight() / 2;
                double d = Math.hypot(cx - x1, cy - y1);
                if (d < bestDist) {
                    bestDist = d;
                    best = new BeamHit(e, cx, cy);
                }
            }
        }
        return best;
    }

    private static class BeamHit {
        final Enemy enemy; final int hitX, hitY;
        BeamHit(Enemy enemy, int hitX, int hitY) { this.enemy = enemy; this.hitX = hitX; this.hitY = hitY; }
    }

    private static class Segment {
        final int x1,y1,x2,y2; final boolean primary;
        Segment(int x1,int y1,int x2,int y2,boolean primary){this.x1=x1;this.y1=y1;this.x2=x2;this.y2=y2;this.primary=primary;}
    }

    private static class ArcBeamVisual extends VisualObject {
        private final List<Segment> segments = new ArrayList<>();
        boolean added = false;
        ArcBeamVisual() { super(0,0,1,1, Color.CYAN); }
        void setSegments(List<Segment> segs) { segments.clear(); segments.addAll(segs); }
        void clear() { segments.clear(); }
        void setBeamVisible(boolean v) { this.visible = v; }
        @Override public void update(int tickCount) { /* lifetime managed by ultimate */ }
        @Override public void render(Graphics2D g, int tickCount) { draw(g); }
        @Override public void draw(Graphics g) {
            if (!visible || segments.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g;
            for (Segment s : segments) {
                g2.setStroke(new BasicStroke(s.primary ? 5f : 3f));
                g2.setColor(s.primary ? new Color(170, 240, 255, 230) : new Color(120, 180, 255, 200));
                g2.drawLine(s.x1, s.y1, s.x2, s.y2);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(Color.WHITE);
                g2.drawLine(s.x1, s.y1, s.x2, s.y2);
            }
            g2.setStroke(new BasicStroke(1f));
        }
    }
}
