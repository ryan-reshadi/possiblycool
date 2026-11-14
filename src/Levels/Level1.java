package Levels;

import Objects.DroppedItem;
import Objects.EnemyClasses.BossEnemy;
import Objects.EnemyClasses.Enemy;
import Objects.EnemyClasses.MeleeEnemy;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Armor;
import Objects.PlayerEquipment.Armor.ArmorSlot;
import Objects.PlayerEquipment.HealthPotion;
import Objects.PlayerEquipment.Item;
import Objects.PlayerEquipment.Weapons.Hammer;
import Objects.Terrain.Border;
import Objects.Terrain.BossDoor;
import Objects.Terrain.Checkpoint;
import Objects.Terrain.Chest;
import Objects.VisualObject;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Level1 extends BaseLevel {

    private BossDoor bossDoor;
    private final List<Enemy> room2Guards = new ArrayList<>();

    public Level1(int bgX, int bgY, Player player) {
        super(new Color(22, 24, 30), new ArrayList<VisualObject>() {{ add(player); }});
        this.width = 3000;
        this.height = 2000;
        this.initLevel();
    }

    @Override
    protected void initLevel() {
        Color wallColor = new Color(55, 60, 75);

        // ══════════════════════════════════════════════════════════════════════
        // 1. STARTING ROOM (x: 250..650, y: 150..500)
        // ══════════════════════════════════════════════════════════════════════
        addBorder(new Border(250, 150, 400, 20, wallColor)); // Top
        addBorder(new Border(250, 150, 20, 350, wallColor)); // Left
        addBorder(new Border(250, 480, 400, 20, wallColor)); // Bottom
        // Right wall with corridor gap (gap at y: 280..380)
        addBorder(new Border(630, 150, 20, 130, wallColor));
        addBorder(new Border(630, 380, 20, 120, wallColor));

        // Corridor 1: x: 650..850, y: 280..380
        addBorder(new Border(630, 280, 220, 20, wallColor)); // corridor top
        addBorder(new Border(630, 380, 220, 20, wallColor)); // corridor bottom

        // ══════════════════════════════════════════════════════════════════════
        // 2. ROOM 1: COMBAT & CHEST (x: 850..1350, y: 150..600)
        // ══════════════════════════════════════════════════════════════════════
        addBorder(new Border(850, 150, 500, 20, wallColor)); // Top
        addBorder(new Border(850, 150, 20, 150, wallColor)); // Left top
        addBorder(new Border(850, 380, 20, 220, wallColor)); // Left bottom
        addBorder(new Border(850, 580, 500, 20, wallColor)); // Bottom
        // Right wall with corridor gap (gap at y: 320..420)
        addBorder(new Border(1330, 150, 20, 170, wallColor));
        addBorder(new Border(1330, 420, 20, 180, wallColor));

        // Room 1 Enemies (2 Melee Enemies with loot drops)
        spawnLootEnemy(950, 250, new HealthPotion());
        spawnLootEnemy(1100, 420, new Armor("Iron Helmet", "Provides 15% damage reduction.", 0.15, ArmorSlot.HEAD));

        // Room 1 Chest (Contains Hammer and Body Armor)
        List<Item> chestLoot = Arrays.asList(
            new Hammer(),
            new Armor("Steel Chestplate", "Heavy armor. 30% damage reduction.", 0.30, ArmorSlot.BODY),
            new HealthPotion()
        );
        Chest chest = new Chest(1250, 200, chestLoot, items -> {
            for (int i = 0; i < items.size(); i++) {
                Item it = items.get(i);
                queueAddVisualObject(new DroppedItem(1230 + (i * 30), 250, it, player::addToHotbar));
            }
        });
        queueAddVisualObject(chest);

        // Corridor 2: x: 1350..1550, y: 320..420
        addBorder(new Border(1330, 320, 220, 20, wallColor)); // top
        addBorder(new Border(1330, 420, 220, 20, wallColor)); // bottom

        // Checkpoint in alcove
        Checkpoint cp = new Checkpoint(1430, 350, () -> {
            if (player != null) player.saveCheckpoint();
        });
        queueAddVisualObject(cp);

        // ══════════════════════════════════════════════════════════════════════
        // 3. ROOM 2: GUARD ROOM & BOSS DOOR (x: 1550..2050, y: 150..650)
        // ══════════════════════════════════════════════════════════════════════
        addBorder(new Border(1550, 150, 500, 20, wallColor)); // Top
        addBorder(new Border(1550, 150, 20, 190, wallColor)); // Left top
        addBorder(new Border(1550, 420, 20, 230, wallColor)); // Left bottom
        addBorder(new Border(1550, 630, 500, 20, wallColor)); // Bottom

        // Right wall has Boss Door (x: 2030, y: 320..440)
        addBorder(new Border(2030, 150, 20, 170, wallColor)); // Right top
        addBorder(new Border(2030, 440, 20, 210, wallColor)); // Right bottom

        this.bossDoor = new BossDoor(2030, 320, 20, 120, () -> {
            System.out.println("Boss Door has opened!");
        });
        addBorder(this.bossDoor);

        // Room 2 Guards (3 enemies)
        spawnGuard(1700, 250);
        spawnGuard(1800, 380);
        spawnGuard(1720, 500);

        // ══════════════════════════════════════════════════════════════════════
        // 4. BOSS ROOM (x: 2050..2650, y: 150..650)
        // ══════════════════════════════════════════════════════════════════════
        addBorder(new Border(2050, 150, 600, 20, wallColor)); // Top
        addBorder(new Border(2630, 150, 20, 500, wallColor)); // Right
        addBorder(new Border(2050, 630, 600, 20, wallColor)); // Bottom

        // Placeholder Boss Enemy
        BossEnemy boss = new BossEnemy(2350, 350);
        boss.setOnDeath(() -> {
            System.out.println("Boss defeated!");
            queueAddVisualObject(new DroppedItem(2350, 350, new HealthPotion(), player::addToHotbar));
        });
        addEnemy(boss);
    }

    private void spawnLootEnemy(int x, int y, Item dropItem) {
        MeleeEnemy e = new MeleeEnemy(x, y);
        e.setOnDeath(() -> {
            if (dropItem != null) {
                queueAddVisualObject(new DroppedItem(e.getX(), e.getY(), dropItem, player::addToHotbar));
            }
        });
        addEnemy(e);
    }

    private void spawnGuard(int x, int y) {
        MeleeEnemy e = new MeleeEnemy(x, y);
        room2Guards.add(e);
        e.setOnDeath(() -> {
            room2Guards.remove(e);
            queueAddVisualObject(new DroppedItem(e.getX(), e.getY(), new HealthPotion(), player::addToHotbar));
            if (room2Guards.isEmpty() && bossDoor != null && bossDoor.isLocked()) {
                bossDoor.unlock();
            }
        });
        addEnemy(e);
    }
}
