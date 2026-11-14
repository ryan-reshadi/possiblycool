package Objects.Terrain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import Objects.PlayerClasses.RoguePlayer;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.HealthPotion;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChestInteractionTest {
    @Test
    void opensOnlyWhenClickedInRange() {
        Player player = new RoguePlayer(0, 0);
        Chest chest = new Chest(50, 50, List.of(new HealthPotion()), null);

        assertFalse(chest.tryOpen(player, 70, 40));
        assertFalse(chest.tryOpen(new RoguePlayer(500, 500), 60, 60));
        assertTrue(chest.tryOpen(player, 60, 60));
        assertTrue(chest.isOpened());
    }
}
