package Objects.PlayerEquipment.Abilities;

import Objects.PlayerEquipment.Ability;
import Objects.PlayerClasses.Player;
import Objects.EnemyClasses.Enemy;
import Levels.BaseLevel;

import java.util.List;

public class RogueShadowStep extends Ability {

    private static final int COOLDOWN = 180; // 3 seconds
    private static final double DASH_DISTANCE = 150.0;
    private static final int DAMAGE = 20;
    private static final int STEP_SIZE = 10;

    public RogueShadowStep() {
        super(COOLDOWN);
    }

    @Override
    public void activate(Player player, BaseLevel level, int currentTick) {
        if (!isReady(currentTick)) return;
        lastUsedTick = currentTick;

        int playerCX = player.getX() + player.getWidth() / 2;
        int playerCY = player.getY() + player.getHeight() / 2;

        double dx = targetX - playerCX;
        double dy = targetY - playerCY;
        double dist = Math.hypot(dx, dy);

        double nx = dist > 0 ? dx / dist : 1.0;
        double ny = dist > 0 ? dy / dist : 0.0;

        List<Enemy> enemies = level.getEnemies();
        int steps = (int) (DASH_DISTANCE / STEP_SIZE);
        for (int s = 1; s <= steps; s++) {
            int checkX = playerCX + (int) (nx * STEP_SIZE * s) - player.getWidth() / 2;
            int checkY = playerCY + (int) (ny * STEP_SIZE * s) - player.getHeight() / 2;

            if (enemies != null) {
                for (Enemy enemy : enemies) {
                    if (enemy == null || enemy.isDead()) continue;
                    boolean overlapX = checkX < enemy.getX() + enemy.getWidth() && checkX + player.getWidth() > enemy.getX();
                    boolean overlapY = checkY < enemy.getY() + enemy.getHeight() && checkY + player.getHeight() > enemy.getY();
                    if (overlapX && overlapY) {
                        enemy.hit(DAMAGE, playerCX, playerCY, 5.0, currentTick);
                    }
                }
            }
        }

        int dashDX = (int) (nx * DASH_DISTANCE);
        int dashDY = (int) (ny * DASH_DISTANCE);
        level.movePlayerInWorld(dashDX, dashDY);
    }

    @Override
    public String getName() {
        return "Shadow Step";
    }

    @Override
    public String getDescription() {
        return "Dash 150px toward cursor, dealing " + DAMAGE + " damage to enemies in the path. Cooldown: 3s";
    }
}
