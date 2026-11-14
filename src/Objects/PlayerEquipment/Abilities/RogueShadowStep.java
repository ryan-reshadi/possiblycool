package Objects.PlayerEquipment.Abilities;

import Objects.PlayerEquipment.Ability;
import Objects.PlayerClasses.Player;
import Objects.EnemyClasses.Enemy;
import Levels.BaseLevel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RogueShadowStep extends Ability {

    private static final int COOLDOWN = 180; // 3 seconds
    private static final double DASH_DISTANCE = 150.0;
    private static final int DAMAGE = 20;

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

        int startX = player.getX();
        int startY = player.getY();
        int dashDX = (int) (nx * DASH_DISTANCE);
        int dashDY = (int) (ny * DASH_DISTANCE);
        level.movePlayerInWorld(dashDX, dashDY);

        int movedX = player.getX() - startX;
        int movedY = player.getY() - startY;
        int steps = Math.max(Math.abs(movedX), Math.abs(movedY));
        List<Enemy> enemies = level.getEnemies();
        Set<Enemy> hitEnemies = new HashSet<>();
        for (int step = 1; step <= steps; step++) {
            int checkX = startX + (int) Math.round(movedX * (step / (double) steps));
            int checkY = startY + (int) Math.round(movedY * (step / (double) steps));
            for (Enemy enemy : enemies) {
                if (enemy == null || enemy.isDead() || hitEnemies.contains(enemy)) continue;
                boolean overlapX = checkX < enemy.getX() + enemy.getWidth()
                        && checkX + player.getWidth() > enemy.getX();
                boolean overlapY = checkY < enemy.getY() + enemy.getHeight()
                        && checkY + player.getHeight() > enemy.getY();
                if (overlapX && overlapY) {
                    enemy.hit(DAMAGE, playerCX, playerCY, 5.0, currentTick);
                    hitEnemies.add(enemy);
                }
            }
        }
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
