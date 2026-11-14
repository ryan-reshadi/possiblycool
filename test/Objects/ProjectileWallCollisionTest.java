package Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import Objects.EnemyClasses.MeleeEnemy;
import Objects.Terrain.Border;
import Objects.Terrain.BossDoor;
import java.awt.Color;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProjectileWallCollisionTest {
    @Test
    void playerProjectileStopsAtWallBeforeDamagingEnemy() {
        MeleeEnemy enemy = new MeleeEnemy(60, 40);
        boolean[] removed = {false};
        Projectile projectile = new Projectile(10, 45, 0, 30, 20, Color.YELLOW,
                () -> removed[0] = true);
        projectile.setEnemies(List.of(enemy));
        projectile.setWalls(List.of(new Border(30, 0, 10, 100, Color.GRAY)));

        projectile.update(1);

        assertTrue(removed[0]);
        assertEquals(enemy.getMaxHealth(), enemy.getHealth());
    }

    @Test
    void openBossDoorDoesNotBlockProjectiles() {
        BossDoor door = new BossDoor(30, 0, 10, 100, null);
        door.unlock();

        assertFalse(ProjectileCollision.hitsWall(5, 50, 50, 50, 10, 10, List.of(door)));
    }
}
