package Levels.Generation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GenerationTests {

    @Test
    public void combatGenerationIsDeterministic() {
        GenerationContext ctx1 = GenerationContext.combat("c1", 3, 8, 12345L);
        GenerationContext ctx2 = GenerationContext.combat("c1", 3, 8, 12345L);
        LevelBlueprint bp1 = LayoutGenerator.generateCombat(ctx1);
        LevelBlueprint bp2 = LayoutGenerator.generateCombat(ctx2);
        assertEquals(bp1.width, bp2.width);
        assertEquals(bp1.height, bp2.height);
        assertEquals(bp1.rooms.size(), bp2.rooms.size());
        assertNull(bp1.boss);
        assertNull(bp2.boss);
    }

    @Test
    public void combatBlueprintIsReachable() {
        GenerationContext ctx = GenerationContext.combat("c2", 2, 8, 999L);
        LevelBlueprint bp = LayoutGenerator.generateCombat(ctx);
        assertTrue(BlueprintValidator.isReachable(bp), "Combat blueprint should be reachable");
    }

    @Test
    public void bossBlueprintHasPuzzlesAndBoss() {
        GenerationContext ctx = GenerationContext.boss("b1", 8, 555L);
        LevelBlueprint bp = LayoutGenerator.generateBoss(ctx);
        assertNotNull(bp.boss, "Boss should exist");
        assertFalse(bp.puzzleButtons.isEmpty(), "Boss blueprint should have puzzle buttons");
        assertFalse(bp.lockPlacements.isEmpty(), "Boss blueprint should have lock placements");
        assertTrue(BlueprintValidator.isReachable(bp), "Boss blueprint should be reachable");
    }
}
