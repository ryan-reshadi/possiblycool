package Levels;

import GameRun.RunRoute;
import Objects.EnemyClasses.BossEnemy;
import Objects.PlayerClasses.Player;
import Objects.Terrain.Border;
import Objects.VisualObject;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/** Final encounter for a generated route. */
public final class RunBossRoom extends BaseLevel {
    public RunBossRoom(Player player, int stages) {
        super(new Color(30, 14, 18), new ArrayList<VisualObject>(List.of(player)));
        this.width = 1900;
        this.height = 1000;
        Color wall = new Color(85, 45, 50);
        addBorder(new Border(150, 100, 1600, 24, wall));
        addBorder(new Border(150, 876, 1600, 24, wall));
        addBorder(new Border(150, 100, 24, 800, wall));
        addBorder(new Border(1726, 100, 24, 800, wall));
        player.setX(250);
        player.setY(480);
        BossEnemy boss = new BossEnemy(1280, 460);
        boss.setHealth(300 + Math.max(0, stages - RunRoute.DEFAULT_STAGES) * 30);
        addEnemy(boss);
        objective = level -> boss.isDead();
    }
}
