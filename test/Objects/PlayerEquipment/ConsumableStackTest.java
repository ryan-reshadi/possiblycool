package Objects.PlayerEquipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Objects.PlayerClasses.Player;
import Objects.PlayerClasses.RoguePlayer;
import Objects.VisualObject;
import Levels.BaseLevel;
import ShapeCore.InventoryPanel;
import java.awt.Color;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class ConsumableStackTest {

    @Test
    void consumingAnItemAdvancesToTheNextItemsUses() {
        Player player = new RoguePlayer(0, 0);
        player.setHealth(10);
        ConfiguredConsumable stack = new ConfiguredConsumable(2, 5);
        stack.mergeFrom(new ConfiguredConsumable(2, 5));

        assertEquals(2, stack.getStackCount());
        stack.use(player, 0);
        assertEquals(2, stack.getStackCount());
        assertEquals(1, stack.getUsesRemaining());
        assertEquals(15, player.getHealth());

        stack.use(player, 1);
        assertEquals(1, stack.getStackCount());
        assertEquals(2, stack.getUsesRemaining());
        assertEquals(20, player.getHealth());

        stack.use(player, 2);
        stack.use(player, 3);
        assertTrue(stack.isUsed());
        assertEquals(0, stack.getStackCount());
        assertEquals(30, player.getHealth());
    }

    @Test
    void storageMergesCompatibleItemsUpToTheStackLimit() {
        InventoryPanel inventory = new InventoryPanel();
        for (int i = 0; i < Consumable.MAX_STACK_SIZE + 1; i++) {
            inventory.addItem(new HealthPotion());
        }

        assertEquals(2, inventory.getStorage().size());
        assertEquals(Consumable.MAX_STACK_SIZE,
                ((Consumable) inventory.getStorage().get(0)).getStackCount());
        assertEquals(1, ((Consumable) inventory.getStorage().get(1)).getStackCount());
    }

    @Test
    void collectedConsumablesMergeIntoExistingHotbarStacks() {
        Player player = new RoguePlayer(0, 0);
        for (int i = 0; i < player.getInventory().length; i++) {
            player.getInventory()[i] = null;
        }
        HealthPotion existing = new HealthPotion();
        player.getInventory()[0] = existing;
        BaseLevel level = new BaseLevel(Color.BLACK, new ArrayList<>(java.util.List.of(player)));

        level.collectItem(new HealthPotion());

        assertEquals(2, existing.getStackCount());
        assertTrue(level.getInventoryPanel().getStorage().isEmpty());
    }

    @Test
    void collectedConsumableOverflowGoesToInventoryStorage() {
        Player player = new RoguePlayer(0, 0);
        for (int i = 0; i < player.getInventory().length; i++) {
            player.getInventory()[i] = null;
        }
        HealthPotion existing = new HealthPotion();
        for (int i = 1; i < Consumable.MAX_STACK_SIZE; i++) {
            existing.mergeFrom(new HealthPotion());
        }
        player.getInventory()[0] = existing;
        BaseLevel level = new BaseLevel(Color.BLACK, new ArrayList<>(java.util.List.of(player)));

        level.collectItem(new HealthPotion());

        assertEquals(Consumable.MAX_STACK_SIZE, existing.getStackCount());
        assertEquals(1, level.getInventoryPanel().getStorage().size());
        assertEquals(1, ((Consumable) level.getInventoryPanel().getStorage().get(0)).getStackCount());
    }

    @Test
    void hotbarMergesCompatibleConsumablesAndLeavesOverflowWithSource() {
        Player player = new RoguePlayer(0, 0);
        for (int i = 0; i < player.getInventory().length; i++) {
            player.getInventory()[i] = null;
        }
        player.addToHotbar(new HealthPotion());
        for (int i = 1; i < Consumable.MAX_STACK_SIZE - 1; i++) {
            assertTrue(player.addToHotbar(new HealthPotion()));
        }
        for (int i = 1; i < player.getInventory().length; i++) {
            player.getInventory()[i] = new Item() {};
        }

        HealthPotion overflow = new HealthPotion();
        overflow.mergeFrom(new HealthPotion());
        assertFalse(player.addToHotbar(overflow));

        assertEquals(Consumable.MAX_STACK_SIZE,
                ((Consumable) player.getInventory()[0]).getStackCount());
        assertEquals(1, overflow.getStackCount());
    }

    @Test
    void configurableConsumablesOnlyMergeWhenTheirEffectSettingsMatch() {
        ConfiguredConsumable stronger = new ConfiguredConsumable(2, 10);
        ConfiguredConsumable weaker = new ConfiguredConsumable(2, 5);

        assertEquals(0, stronger.mergeFrom(weaker));
        assertEquals(1, stronger.getStackCount());
        assertEquals(1, weaker.getStackCount());
    }

    private static final class ConfiguredConsumable extends Consumable {
        private final int effectAmount;

        private ConfiguredConsumable(int uses, int effectAmount) {
            super(uses);
            this.effectAmount = effectAmount;
            setDescription("Restores " + effectAmount);
        }

        @Override
        public void applyEffect(Player player, int currentTick) {
            player.heal(effectAmount);
        }

        @Override
        protected Object getStackingKey() {
            return effectAmount;
        }
    }
}
