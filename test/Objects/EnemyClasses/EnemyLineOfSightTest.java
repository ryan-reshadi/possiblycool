package Objects.EnemyClasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import Objects.PlayerClasses.Player;
import Objects.Terrain.Border;
import Objects.Terrain.BossDoor;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EnemyLineOfSightTest {
    @Test
    void meleeAndBossCannotHitThroughAWall() {
        Player player = new Player(20, 0, 100);
        Border wall = new Border(30, 0, 10, 100, Color.GRAY);
        MeleeEnemy melee = new MeleeEnemy(0, 0);
        BossEnemy boss = new BossEnemy(0, 0);
        melee.setBorders(List.of(wall));
        boss.setBorders(List.of(wall));

        melee.checkAggro(player);
        boss.checkAggro(player);
        melee.update(1);
        boss.update(1);

        assertEquals(player.getMaxHealth(), player.getHealth());
    }

    @Test
    void rangedWeaponCannotShootThroughLockedDoorButCanShootThroughUnlockedDoor() {
        Player player = new Player(100, 0, 100);
        BossDoor door = new BossDoor(50, 0, 10, 100, null);
        MeleeEnemy enemy = new MeleeEnemy(0, 0);
        List<Object> projectiles = new ArrayList<>();
        enemy.setProjectileSink(projectiles::add);
        enemy.setBorders(List.of(door));
        RangedWeapon weapon = new RangedWeapon();

        weapon.update(enemy, player, 1);
        assertTrue(projectiles.isEmpty());

        door.unlock();
        weapon.update(enemy, player, 2);
        assertEquals(1, projectiles.size());
    }

    @Test
    void areaAttackCannotStartThroughAWall() {
        Player player = new Player(60, 0, 100);
        MeleeEnemy enemy = new MeleeEnemy(0, 0);
        enemy.setBorders(List.of(new Border(40, 0, 10, 100, Color.GRAY)));
        AreaWeapon weapon = new AreaWeapon();

        for (int tick = 1; tick <= 60; tick++) {
            weapon.update(enemy, player, tick);
        }

        assertEquals(player.getMaxHealth(), player.getHealth());
    }

    @Test
    void areaAndTankAttacksCannotDamageAfterAWallBlocksSight() {
        Player player = new Player(20, 0, 100);
        MeleeEnemy areaEnemy = new MeleeEnemy(0, 0);
        MeleeEnemy tankEnemy = new MeleeEnemy(0, 0);
        AreaWeapon areaWeapon = new AreaWeapon();
        TankWeapon tankWeapon = new TankWeapon();

        areaWeapon.update(areaEnemy, player, 1);
        tankWeapon.update(tankEnemy, player, 1);
        assertTrue(tankWeapon.isAttacking());

        Border wall = new Border(30, 0, 10, 100, Color.GRAY);
        areaEnemy.setBorders(List.of(wall));
        tankEnemy.setBorders(List.of(wall));
        for (int tick = 2; tick <= 60; tick++) {
            areaWeapon.update(areaEnemy, player, tick);
            tankWeapon.update(tankEnemy, player, tick);
        }

        assertEquals(player.getMaxHealth(), player.getHealth());
    }

    @Test
    void tankAndBerserkerAttacksDoNotStartThroughAWall() {
        Player player = new Player(20, 0, 100);
        Border wall = new Border(30, 0, 10, 100, Color.GRAY);
        MeleeEnemy tank = new MeleeEnemy(0, 0);
        MeleeEnemy berserker = new MeleeEnemy(0, 0);
        tank.setBorders(List.of(wall));
        berserker.setBorders(List.of(wall));
        TankWeapon tankWeapon = new TankWeapon();
        BerserkerWeapon berserkerWeapon = new BerserkerWeapon();
        assertFalse(tank.hasLineOfSightTo(player));
        assertFalse(berserker.hasLineOfSightTo(player));

        for (int tick = 1; tick <= 60; tick++) {
            tankWeapon.update(tank, player, tick);
        }
        assertEquals(player.getMaxHealth(), player.getHealth());

        for (int tick = 1; tick <= 60; tick++) {
            berserkerWeapon.update(berserker, player, tick);
        }

        assertEquals(player.getMaxHealth(), player.getHealth());
        assertEquals(0, berserker.dashTicks);
    }
}
