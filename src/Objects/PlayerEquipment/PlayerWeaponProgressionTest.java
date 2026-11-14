package Objects.PlayerEquipment;

import Objects.EnemyClasses.TankWeapon;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Spells.FireBoltSpell;
import Objects.PlayerEquipment.Weapons.Dagger;
import Objects.PlayerEquipment.Weapons.Wand;
import java.util.Random;

public final class PlayerWeaponProgressionTest {
    private PlayerWeaponProgressionTest() {
    }

    public static void main(String[] args) {
        testDamageRollAndLevelScaling();
        testBiasedLevelRolls();
        testLootUsesEquippedWeaponLevel();
        testPlayerWeaponStats();
        testEnemyWeaponsRemainUnchanged();
    }

    private static void testDamageRollAndLevelScaling() {
        TestWeapon weapon = new TestWeapon(0.9);
        TestWeapon maximumRoll = new TestWeapon(1.1);
        assertEquals(16, weapon.getScaledDamage(18), "minimum damage roll");
        assertEquals(16, weapon.getScaledDamage(18), "persistent damage roll");
        assertEquals(20, maximumRoll.getScaledDamage(18), "maximum level 1 damage");
        weapon.setLevel(2);
        assertEquals(21, weapon.getScaledDamage(18), "minimum level 2 damage");

        weapon.setLevel(1);
        assertEquals(90, weapon.getScaledDamage(100), "minimum damage roll");
        weapon.setLevel(3);
        assertEquals(132, weapon.getScaledDamage(100), "damage scaling by level");
        maximumRoll.setLevel(3);
        assertEquals(152, maximumRoll.getScaledDamage(100), "maximum damage roll scales by level");
        weapon.setLevel(0);
        assertEquals(1, weapon.getLevel(), "minimum weapon level");
    }

    private static void testBiasedLevelRolls() {
        int lower = 0;
        int same = 0;
        int higher = 0;
        for (int roll = 0; roll < 100; roll++) {
            int level = PlayerWeapon.rollLevelNear(5, new FixedRandom(roll));
            if (level == 4) lower++;
            else if (level == 5) same++;
            else if (level == 6) higher++;
            else throw new AssertionError("Unexpected level offset: " + level);
        }
        assertEquals(15, lower, "lower-level weight");
        assertEquals(35, same, "same-level weight");
        assertEquals(50, higher, "higher-level weight");
        assertEquals(1, PlayerWeapon.rollLevelNear(1, new FixedRandom(0)), "minimum drop level");
    }

    private static void testLootUsesEquippedWeaponLevel() {
        Player player = new Player(0, 0, 100);
        TestWeapon equipped = new TestWeapon(1.0);
        equipped.setLevel(5);
        player.addToHotbar(equipped);
        player.setMainHandIndex(0);

        LootTable table = LootTable.of(LootTable.item(1, () -> new TestWeapon(1.0)));
        for (int i = 0; i < 100; i++) {
            int level = ((PlayerWeapon) table.roll(player, 1).get(0)).getLevel();
            if (level < 4 || level > 6) {
                throw new AssertionError("Drop level was not near equipped weapon level: " + level);
            }
        }
    }

    private static void testPlayerWeaponStats() {
        Dagger dagger = new Dagger();
        if (dagger.getAttackRange() != 90 || dagger.getAttacksPerSecond() <= 0) {
            throw new AssertionError("Melee weapon combat stats should be available.");
        }

        Wand wand = new Wand();
        wand.setActiveSpell(new FireBoltSpell());
        wand.setLevel(2);
        if (wand.getDamage() < 26 || wand.getDamage() > 32
                || wand.getAttacksPerSecond() <= 0 || wand.getEffectiveRange() != 1440) {
            throw new AssertionError("Wand progression or combat stats are incorrect.");
        }
    }

    private static void testEnemyWeaponsRemainUnchanged() {
        if (new TankWeapon().getProfile().damage != 12) {
            throw new AssertionError("Enemy weapon base damage should remain unchanged.");
        }
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static final class TestWeapon extends PlayerWeapon {
        TestWeapon(double damageRoll) {
            super(damageRoll);
        }
    }

    private static final class FixedRandom extends Random {
        private final int value;

        FixedRandom(int value) {
            this.value = value;
        }

        @Override
        public int nextInt(int bound) {
            return value;
        }
    }
}
