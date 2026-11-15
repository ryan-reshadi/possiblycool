package Levels;

import Objects.EnemyClasses.BossEnemy;
import Objects.EnemyClasses.MeleeEnemy;
import Objects.PlayerClasses.Player;
import Objects.Terrain.Border;
import Objects.VisualObject;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/** Small reusable combat room used by generated route nodes. */
public final class RunCombatRoom extends BaseLevel {
    public RunCombatRoom(Player player, int stage, long seed) {
        super(new Color(22, 24, 30), new ArrayList<VisualObject>(List.of(player)));
        this.width = 1900;
        this.height = 1000;
        Color wall = new Color(65, 70, 82);
        addBorder(new Border(180, 120, 1540, 24, wall));
        addBorder(new Border(180, 856, 1540, 24, wall));
        addBorder(new Border(180, 120, 24, 760, wall));
        addBorder(new Border(1696, 120, 24, 760, wall));
        player.setX(260);
        player.setY(480);
        java.util.Random random = new java.util.Random(seed);
        int count = Math.min(5, 2 + stage / 2);
        for (int i = 0; i < count; i++) {
            int x = 760 + i * 170;
            int y = 280 + random.nextInt(360);
            int health = 55 + stage * 8;
            addEnemy(new MeleeEnemy(x, y, health, 8 + stage, 350, 2, 42));
        }
        objective = level -> level.getEnemies().isEmpty();
    }
}
