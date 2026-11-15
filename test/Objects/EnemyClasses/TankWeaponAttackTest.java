package Objects.EnemyClasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import Objects.PlayerClasses.RoguePlayer;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class TankWeaponAttackTest {
    @Test
    void enemyUpdateChasesIntoRangeAndDealsDamage() {
        TankWeapon weapon = new TankWeapon();
        MeleeEnemy enemy = new MeleeEnemy(250, 100);
        RoguePlayer player = new RoguePlayer(100, 100);
        enemy.setWeapon(weapon);

        for (int tick = 1; tick <= 60; tick++) {
            enemy.checkAggro(player);
            enemy.update(tick);
        }

        assertTrue(enemy.x < 250, "The tank should pursue at its movement speed.");
        assertTrue(player.getHealth() < player.getMaxHealth(),
                "The normal enemy update path should eventually apply melee damage.");
    }

    @Test
    void takingAHitCancelsTheCurrentSwing() {
        TankWeapon weapon = new TankWeapon();
        MeleeEnemy enemy = new MeleeEnemy(100, 100);
        RoguePlayer player = new RoguePlayer(145, 105);
        enemy.setWeapon(weapon);
        enemy.checkAggro(player);
        enemy.update(1);

        assertTrue(weapon.isAttacking());
        enemy.hit(1, player.getX(), player.getY(), 8, 2);

        assertFalse(weapon.isAttacking());
        assertNull(weapon.getAttackArc());
    }

    @Test
    void tankEnemySwordGivesPlayerLongerReactionWindow() {
        TankEnemySword sword = new TankEnemySword();
        MeleeEnemy enemy = new MeleeEnemy(100, 100);
        RoguePlayer player = new RoguePlayer(145, 105);
        enemy.setWeapon(sword);
        enemy.checkAggro(player);

        enemy.update(0);
        assertTrue(sword.isAttacking());
        assertEquals(72 * 2.0, sword.getAttackArc().getWidth());

        for (int tick = 1; tick <= 21; tick++) {
            enemy.update(tick);
        }
        assertEquals(player.getMaxHealth(), player.getHealth(),
                "The sword should telegraph for at least 21 ticks before its first hit.");

        enemy.update(22);
        assertEquals(player.getMaxHealth() - 10, player.getHealth());
    }

    @Test
    void tankWeaponShowsAndExecutesTankSwordStyleCombo() {
        TankWeapon weapon = new TankWeapon();
        MeleeEnemy enemy = new MeleeEnemy(100, 100);
        RoguePlayer player = new RoguePlayer(145, 105);
        enemy.setWeapon(weapon);
        enemy.checkAggro(player);

        enemy.update(0);

        assertTrue(weapon.isAttacking());
        assertArc(weapon, 72, 44);
        BufferedImage image = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            enemy.render(graphics, 0);
        } finally {
            graphics.dispose();
        }
        assertTrue((image.getRGB(155, 127) >>> 24) > 0, "The active swing should render its attack arc.");

        for (int tick = 1; tick <= 6; tick++) {
            weapon.update(enemy, player, tick);
        }
        assertEquals(75, player.getHealth(), "Damage should wait for the first move's damage frame.");
        weapon.update(enemy, player, 7);
        assertEquals(65, player.getHealth());
        finishAttack(weapon, enemy, player, 8, 19);
        assertFalse(weapon.isAttacking());
        assertNull(weapon.getAttackArc());

        weapon.update(enemy, player, 124);
        assertArc(weapon, 88, 62);
        for (int tick = 125; tick <= 133; tick++) {
            weapon.update(enemy, player, tick);
        }
        assertEquals(65, player.getHealth(), "The second move should use its longer damage delay.");
        weapon.update(enemy, player, 134);
        assertEquals(53, player.getHealth());
        finishAttack(weapon, enemy, player, 135, 149);
        assertFalse(weapon.isAttacking());

        weapon.update(enemy, player, 254);
        assertArc(weapon, 104, 88);
        for (int tick = 255; tick <= 268; tick++) {
            weapon.update(enemy, player, tick);
        }
        assertEquals(53, player.getHealth(), "The finisher should use its longest damage delay.");
        weapon.update(enemy, player, 269);
        assertEquals(36, player.getHealth());
        finishAttack(weapon, enemy, player, 270, 286);
        assertFalse(weapon.isAttacking());
    }

    private static void assertArc(TankWeapon weapon, int range, int angle) {
        assertNotNull(weapon.getAttackArc());
        assertEquals(range * 2.0, weapon.getAttackArc().getWidth());
        assertEquals(angle, weapon.getAttackArc().getAngleExtent());
    }

    private static void finishAttack(TankWeapon weapon, MeleeEnemy enemy, RoguePlayer player,
                                     int firstTick, int finalTick) {
        for (int tick = firstTick; tick <= finalTick; tick++) {
            weapon.update(enemy, player, tick);
        }
    }
}
