package Objects.PlayerEquipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import Objects.PlayerClasses.RoguePlayer;
import Objects.EnemyClasses.MeleeEnemy;
import Levels.Level1;
import org.junit.jupiter.api.Test;

class RogueShadowStepWallCollisionTest {
    @Test
    void shadowStepStopsAtStartingRoomWall() {
        RoguePlayer player = new RoguePlayer(400, 300);
        Level1 level = new Level1(0, 0, player);
        MeleeEnemy enemyBehindWall = new MeleeEnemy(200, 300);
        level.addEnemy(enemyBehindWall);
        int enemyHealth = enemyBehindWall.getHealth();

        player.getAbilitySlots()[0].setTarget(0, 325);
        player.getAbilitySlots()[0].activate(player, level, 1);

        assertEquals(270, player.getX());
        assertEquals(300, player.getY());
        assertEquals(enemyHealth, enemyBehindWall.getHealth());
    }
}
