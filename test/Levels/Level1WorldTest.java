package Levels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import Objects.PlayerClasses.RoguePlayer;
import Objects.PlayerClasses.Player;
import org.junit.jupiter.api.Test;

class Level1WorldTest {
    @Test
    void playerStartsInTheStartingRoomAndCameraDoesNotMutateWorldObjects() {
        Player player = new RoguePlayer(400, 300);
        Level1 level = new Level1(0, 0, player);

        assertEquals(400, player.getX());
        assertEquals(300, player.getY());
        int enemyX = level.getEnemies().get(0).getX();
        int enemyY = level.getEnemies().get(0).getY();

        player.setX(2200);
        player.setY(1000);
        level.movePlayerInWorld(200, 100);

        assertEquals(2400, player.getX());
        assertEquals(1100, player.getY());
        assertEquals(enemyX, level.getEnemies().get(0).getX());
        assertEquals(enemyY, level.getEnemies().get(0).getY());
        assertEquals(1100, level.getCameraX());
        assertEquals(625, level.getCameraY());
    }
}
