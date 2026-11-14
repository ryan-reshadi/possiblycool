package Levels;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import Objects.EnemyClasses.BossEnemy;
import org.junit.jupiter.api.Test;

class LevelObjectiveTest {
    @Test
    void levelOneCompletesWhenItsBossIsDefeated() {
        Level1 level = new Level1(0, 0, null);
        BossEnemy boss = level.getEnemies().stream()
                .filter(BossEnemy.class::isInstance)
                .map(BossEnemy.class::cast)
                .findFirst()
                .orElseThrow();

        assertFalse(level.isComplete());
        boss.hit(100_000, boss.getX(), boss.getY(), 0, 0);
        assertTrue(level.isComplete());
    }

    @Test
    void objectiveCanRepresentConditionsOtherThanBossDefeat() {
        ResourceObjectiveLevel level = new ResourceObjectiveLevel();

        assertFalse(level.isComplete());
        level.materialsCollected = true;
        assertTrue(level.isComplete());
    }

    private static final class ResourceObjectiveLevel extends BaseLevel {
        private boolean materialsCollected;

        private ResourceObjectiveLevel() {
            super(java.awt.Color.BLACK, new java.util.ArrayList<>());
            objective = ignored -> materialsCollected;
        }
    }
}
