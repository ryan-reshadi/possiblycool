package Objects.PlayerEquipment.Abilities;

import Levels.BaseLevel;
import Objects.EnemyClasses.Enemy;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Ability;

import java.awt.Color;
import java.util.List;

public class WindGustAbility extends Ability {

    private static final int COOLDOWN = 360; // 6 seconds
    private static final int RANGE = 250;
    private static final int DAMAGE = 20;
    private static final int STUN_TICKS = 60;
    private static final double CONE_HALF_ANGLE_DEG = 30.0;

    public WindGustAbility() {
        super(COOLDOWN);
        this.iconColor = new Color(100, 200, 230);
    }

    @Override
    public void activate(Player player, BaseLevel level, int currentTick) {
        if (!isReady(currentTick)) return;
        lastUsedTick = currentTick;

        int px = player.getX() + player.getWidth() / 2;
        int py = player.getY() + player.getHeight() / 2;
        double aim = Math.atan2(targetY - py, targetX - px);
        List<Enemy> enemies = level.getEnemies();
        if (enemies == null) return;
        for (Enemy enemy : enemies) {
            if (enemy == null || enemy.isDead()) continue;
            int ex = enemy.getX() + enemy.getWidth() / 2;
            int ey = enemy.getY() + enemy.getHeight() / 2;
            double dx = ex - px, dy = ey - py;
            double distance = Math.hypot(dx, dy);
            double delta = Math.atan2(Math.sin(Math.atan2(dy, dx) - aim), Math.cos(Math.atan2(dy, dx) - aim));
            if (distance <= RANGE && Math.abs(delta) <= Math.toRadians(CONE_HALF_ANGLE_DEG)) {
                int previousHealth = enemy.getHealth();
                enemy.hit(DAMAGE, px, py, 80, currentTick);
                enemy.stun(STUN_TICKS, currentTick);
                player.chargeUltimateForDamage(previousHealth - enemy.getHealth());
            }
        }
    }

    @Override
    public String getName() {
        return "Wind Gust";
    }

    @Override
    public String getDescription() {
        return "Blast a 250px cone for 20 damage, knockback, and a 1-second stun. Cooldown: 6s";
    }
}
