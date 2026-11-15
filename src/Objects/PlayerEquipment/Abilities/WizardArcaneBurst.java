package Objects.PlayerEquipment.Abilities;

import Objects.PlayerEquipment.Ability;
import Objects.PlayerClasses.Player;
import Objects.EnemyClasses.Enemy;
import Objects.Effects.ArcaneExplosionEffect;
import Levels.BaseLevel;

import java.util.List;

public class WizardArcaneBurst extends Ability {

    private static final int COOLDOWN = 360; // 6 seconds
    private static final int RADIUS = 120;
    private static final int DAMAGE = 40;

    public WizardArcaneBurst() {
        super(COOLDOWN);
    }

    @Override
    public void activate(Player player, BaseLevel level, int currentTick) {
        if (!isReady(currentTick)) return;
        lastUsedTick = currentTick;

        List<Enemy> enemies = level.getEnemies();
        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy == null || enemy.isDead()) continue;
                int enemyCX = enemy.getX() + enemy.getWidth() / 2;
                int enemyCY = enemy.getY() + enemy.getHeight() / 2;

                double dist = Math.hypot(enemyCX - targetX, enemyCY - targetY);
                if (dist <= RADIUS) {
                    int previousHealth = enemy.getHealth();
                    enemy.hit(DAMAGE, targetX, targetY, 8.0, currentTick);
                    player.chargeUltimateForDamage(previousHealth - enemy.getHealth());
                }
            }
        }

        final ArcaneExplosionEffect[] ref = new ArcaneExplosionEffect[1];
        ref[0] = new ArcaneExplosionEffect(
            targetX, targetY, RADIUS,
            () -> level.queueRemoveVisualObject(ref[0])
        );
        level.queueAddVisualObject(ref[0]);
    }

    @Override
    public String getName() {
        return "Arcane Burst";
    }

    @Override
    public String getDescription() {
        return "Explosion at cursor, " + RADIUS + "px radius, " + DAMAGE + " damage. Cooldown: 6s";
    }
}
