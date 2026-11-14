package Objects.PlayerEquipment;

import Objects.PlayerClasses.Player;
import Objects.PlayerClasses.RoguePlayer;
import Objects.PlayerClasses.TankPlayer;
import Objects.PlayerClasses.WizardPlayer;
import Objects.PlayerEquipment.Weapons.BasicWand;
import ShapeCore.InventoryPanel;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassArmorTest {
    private record ArmorCase(Supplier<? extends Armor> factory, Player owner,
                             Armor.ArmorSlot slot, double reduction) {
    }

    @Test
    void armorSetsHaveClassSpecificSlotsAndDamageReduction() {
        Player knight = new TankPlayer(0, 0);
        Player rogue = new RoguePlayer(0, 0);
        Player wizard = new WizardPlayer(0, 0);
        List<Player> players = List.of(knight, rogue, wizard);
        List<ArmorCase> armorCases = List.of(
                new ArmorCase(KnightHelmet::new, knight, Armor.ArmorSlot.HEAD, 0.15),
                new ArmorCase(KnightChestplate::new, knight, Armor.ArmorSlot.BODY, 0.30),
                new ArmorCase(RogueHood::new, rogue, Armor.ArmorSlot.HEAD, 0.10),
                new ArmorCase(RogueTunic::new, rogue, Armor.ArmorSlot.BODY, 0.20),
                new ArmorCase(WizardHat::new, wizard, Armor.ArmorSlot.HEAD, 0.05),
                new ArmorCase(WizardRobe::new, wizard, Armor.ArmorSlot.BODY, 0.10)
        );

        for (ArmorCase armorCase : armorCases) {
            Armor armor = armorCase.factory().get();
            assertEquals(armorCase.slot(), armor.getSlot());
            assertEquals(armorCase.reduction(), armor.getArmorReduction());
            for (Player player : players) {
                assertEquals(player == armorCase.owner(), armor.canBeUsedBy(player));
            }
        }
    }

    @Test
    void lootTableOnlyRollsArmorForItsAllowedClass() {
        LootTable armorLoot = LootTable.of(
                LootTable.item(1, KnightHelmet::new),
                LootTable.item(1, KnightChestplate::new),
                LootTable.item(1, RogueHood::new),
                LootTable.item(1, RogueTunic::new),
                LootTable.item(1, WizardHat::new),
                LootTable.item(1, WizardRobe::new)
        );
        List<Player> players = List.of(
                new TankPlayer(0, 0),
                new RoguePlayer(0, 0),
                new WizardPlayer(0, 0)
        );

        for (Player player : players) {
            List<Item> drops = armorLoot.roll(player, 5);
            assertEquals(5, drops.size());
            for (Item drop : drops) {
                assertTrue(drop.canBeUsedBy(player));
                assertFalse(drop.getAllowedClasses().isEmpty());
            }
        }
    }

    @Test
    void knightSetBonusAddsProtectionAndTurnsOffWhenSetIsBroken() {
        TankPlayer knight = new TankPlayer(0, 0);
        KnightHelmet helmet = new KnightHelmet();
        KnightChestplate chestplate = new KnightChestplate();

        knight.equipHead(helmet);
        assertEquals(0.40, knight.getTotalArmorReduction());
        assertNull(knight.getActiveArmorSet());

        knight.equipBody(chestplate);
        assertEquals(ArmorSet.KNIGHT, knight.getActiveArmorSet());
        assertEquals(0.75, knight.getTotalArmorReduction());

        knight.equipBody(null);
        assertNull(knight.getActiveArmorSet());
        assertEquals(0.40, knight.getTotalArmorReduction());
    }

    @Test
    void rogueSetBonusIncreasesMovementSpeedOnlyWhileBothPiecesAreEquipped() {
        RoguePlayer rogue = new RoguePlayer(0, 0);
        rogue.equipHead(new RogueHood());
        assertEquals(5.0, rogue.getSpeed());

        rogue.equipBody(new RogueTunic());
        assertEquals(ArmorSet.ROGUE, rogue.getActiveArmorSet());
        assertEquals(6.0, rogue.getSpeed());

        rogue.equipHead(null);
        assertEquals(5.0, rogue.getSpeed());
    }

    @Test
    void wizardSetBonusIncreasesWandDamageAndRetainsInnateProtection() {
        WizardPlayer wizard = new WizardPlayer(0, 0);
        BasicWand wand = new BasicWand();
        int baseDamage = wand.getDamage();

        wizard.equipHead(new WizardHat());
        assertEquals(1.0, wizard.getSpellDamageMultiplier());
        wizard.equipBody(new WizardRobe());

        assertEquals(ArmorSet.WIZARD, wizard.getActiveArmorSet());
        assertEquals(1.15, wizard.getSpellDamageMultiplier());
        assertEquals(Math.round(baseDamage * 1.15), wand.getDamage(wizard));
        assertEquals(0.25, wizard.getTotalArmorReduction());

        wizard.equipBody(null);
        assertEquals(1.0, wizard.getSpellDamageMultiplier());
    }

    @Test
    void inventoryDrawsEquippedSetProgressAndArmorTooltip() {
        RoguePlayer rogue = new RoguePlayer(0, 0);
        rogue.equipHead(new RogueHood());
        rogue.setInventoryOpen(true);
        InventoryPanel panel = new InventoryPanel();
        BufferedImage image = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            panel.draw(graphics, rogue, 269, 188, image.getWidth(), image.getHeight());
        } finally {
            graphics.dispose();
        }
    }
}
