package Objects.PlayerEquipment.Abilities;

import Objects.PlayerEquipment.Ability;
import Objects.PlayerClasses.Player;
import Objects.EnemyClasses.Enemy;
import Levels.BaseLevel;

import java.util.List;

public class TankShieldBash extends Ability {

    private static final int COOLDOWN = 300; // 5 seconds
    private static final double PUSH_RANGE = 100.0;
    private static final double PUSH_DISTANCE = 60.0;
    private static final int DAMAGE = 15;

    public TankShieldBash() {
        super(COOLDOWN);
    }

    @Override
    public void activate(Player player, BaseLevel level, int currentTick) {
        if (!isReady(currentTick)) return;
        lastUsedTick = currentTick;

        int playerCX = player.getX() + player.getWidth() / 2;
        int playerCY = player.getY() + player.getHeight() / 2;

        List<Enemy> enemies = level.getEnemies();
        if (enemies == null) return;

        for (Enemy enemy : enemies) {
            if (enemy == null || enemy.isDead()) continue;
            int enemyCX = enemy.getX() + enemy.getWidth() / 2;
            int enemyCY = enemy.getY() + enemy.getHeight() / 2;

            double dx = enemyCX - playerCX;
            double dy = enemyCY - playerCY;
            double dist = Math.hypot(dx, dy);

            if (dist <= PUSH_RANGE) {
                if (dist > 0) {
                    int previousHealth = enemy.getHealth();
                    enemy.hit(DAMAGE, playerCX, playerCY, PUSH_DISTANCE, currentTick);
                    player.chargeUltimateForDamage(previousHealth - enemy.getHealth());
                } else {
                    int previousHealth = enemy.getHealth();
                    enemy.hit(DAMAGE, playerCX - 1, playerCY, PUSH_DISTANCE, currentTick);
                    player.chargeUltimateForDamage(previousHealth - enemy.getHealth());
                }
                enemy.stun(90, currentTick);
            }
        }
    }

    @Override
    public String getName() {
        return "Shield Bash";
    }

    @Override
    public String getDescription() {
        return "Pushes all nearby enemies away, stuns them, and deals " + DAMAGE + " damage. Cooldown: 5s";
    }
}
