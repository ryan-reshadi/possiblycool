package Objects.EnemyClasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import Objects.PlayerClasses.RoguePlayer;
import Objects.Terrain.Border;
import java.awt.Color;
import java.util.List;
import org.junit.jupiter.api.Test;

class EnemyProjectileWallCollisionTest {
    @Test
    void enemyProjectileStopsAtWallBeforeDamagingPlayer() {
        RoguePlayer player = new RoguePlayer(100, 40);
        EnemyProjectile projectile = new EnemyProjectile(10, 45, player, 20, 40,
                List.of(new Border(30, 0, 10, 100, Color.GRAY)));

        projectile.update(1);

        assertFalse(projectile.isVisible());
        assertEquals(player.getMaxHealth(), player.getHealth());
    }
}
