package Levels;

import Levels.Generation.GenerationContext;
import Levels.Generation.LayoutGenerator;
import Levels.Generation.LevelBlueprint;
import Levels.Generation.Rect;
import Objects.DroppedItem;
import Objects.EnemyClasses.BossEnemy;
import Objects.EnemyClasses.BerserkerWeapon;
import Objects.EnemyClasses.Enemy;
import Objects.EnemyClasses.MeleeEnemy;
import Objects.EnemyClasses.RangedWeapon;
import Objects.EnemyClasses.TankEnemySword;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.HealthPotion;
import Objects.PlayerEquipment.Item;
import Objects.PlayerEquipment.KnightChestplate;
import Objects.PlayerEquipment.KnightHelmet;
import Objects.PlayerEquipment.LootTable;
import Objects.PlayerEquipment.RogueHood;
import Objects.PlayerEquipment.RogueTunic;
import Objects.PlayerEquipment.Weapons.BasicWand;
import Objects.PlayerEquipment.Weapons.Dagger;
import Objects.PlayerEquipment.Weapons.Hammer;
import Objects.PlayerEquipment.WizardHat;
import Objects.PlayerEquipment.WizardRobe;
import Objects.Terrain.Border;
import Objects.Terrain.BossDoor;
import Objects.Terrain.Checkpoint;
import Objects.Terrain.Chest;
import Objects.Terrain.KeyPickup;
import Objects.Terrain.MazeExitPortal;
import Objects.Terrain.PuzzleButton;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Builds multi-room generated run levels from {@link LevelBlueprint}s. */
final class RunGeneratedBuilder {
    private RunGeneratedBuilder() {}

    static void buildCombat(BaseLevel level, Player player, int stage, long seed) {
        GenerationContext ctx = GenerationContext.combat("combat-" + stage, stage, Math.max(stage, 8), seed);
        LevelBlueprint bp = LayoutGenerator.generateCombat(ctx);
        apply(level, player, bp, ctx);
    }

    static void buildBoss(BaseLevel level, Player player, int stages, long seed) {
        GenerationContext ctx = GenerationContext.boss("final-boss", stages, seed);
        LevelBlueprint bp = LayoutGenerator.generateBoss(ctx);
        apply(level, player, bp, ctx);
    }

    private static void apply(BaseLevel level, Player player, LevelBlueprint bp, GenerationContext ctx) {
        level.width = bp.width;
        level.height = bp.height;
        level.bgX = 0;
        level.bgY = 0;
        level.backgroundColor = ctx.boss ? new Color(30, 14, 18) : new Color(22, 24, 30);
        Color wallColor = ctx.boss ? new Color(85, 45, 50) : new Color(65, 70, 82);

        for (Rect w : bp.walls) {
            if (w.w <= 0 || w.h <= 0) continue;
            level.addBorder(new Border(w.x, w.y, w.w, w.h, wallColor));
        }

        List<Enemy> spawned = new ArrayList<>(bp.enemies.size());
        for (LevelBlueprint.EnemySpawn spec : bp.enemies) {
            Enemy enemy = createEnemy(spec, ctx);
            spawned.add(enemy);
            level.addEnemy(enemy);
        }
        attachDefaultDrops(level, spawned, ctx.random);

        for (LevelBlueprint.GateSpec gate : bp.gates) {
            List<Enemy> guards = new ArrayList<>();
            for (int idx : gate.guardEnemyIndices) {
                if (idx >= 0 && idx < spawned.size()) guards.add(spawned.get(idx));
            }
            BossDoor door = new BossDoor(gate.door.x, gate.door.y, gate.door.w, gate.door.h, () -> { });
            if (guards.isEmpty()) {
                door.unlock();
            } else {
                for (Enemy guard : guards) {
                    chainOnDeath(level, guard, () -> {
                        for (Enemy g : guards) if (!g.isDead()) return;
                        if (door.isLocked()) door.unlock();
                    }, ctx.random);
                }
            }
            level.addBorder(door);
        }
        // Additive lock placements
        for (LevelBlueprint.LockPlacement lp : bp.lockPlacements) {
            if (lp == null || lp.lock == null || lp.doorRect == null) continue;
            if (lp.lock instanceof Levels.Generation.LockSpec.KillGuardLock) {
                Levels.Generation.LockSpec.KillGuardLock kgl = (Levels.Generation.LockSpec.KillGuardLock) lp.lock;
                List<Enemy> guards = new ArrayList<>();
                for (int idx : kgl.guardEnemyIndices) {
                    if (idx >= 0 && idx < spawned.size()) guards.add(spawned.get(idx));
                }
                BossDoor door = new BossDoor(lp.doorRect.x, lp.doorRect.y, lp.doorRect.w, lp.doorRect.h, () -> { });
                if (guards.isEmpty()) {
                    door.unlock();
                } else {
                    for (Enemy guard : guards) {
                        chainOnDeath(level, guard, () -> {
                            for (Enemy g : guards) if (!g.isDead()) return;
                            if (door.isLocked()) door.unlock();
                        }, ctx.random);
                    }
                }
                level.addBorder(door);
            } else if (lp.lock instanceof Levels.Generation.LockSpec.PuzzleLock) {
                Levels.Generation.LockSpec.PuzzleLock pl = (Levels.Generation.LockSpec.PuzzleLock) lp.lock;
                BossDoor door = new BossDoor(lp.doorRect.x, lp.doorRect.y, lp.doorRect.w, lp.doorRect.h, () -> { });
                level.addBorder(door);
                level.addPuzzleLock(door, pl.puzzleId);
            } else if (lp.lock instanceof Levels.Generation.LockSpec.ItemKeyLock) {
                Levels.Generation.LockSpec.ItemKeyLock ik = (Levels.Generation.LockSpec.ItemKeyLock) lp.lock;
                BossDoor door = new BossDoor(lp.doorRect.x, lp.doorRect.y, lp.doorRect.w, lp.doorRect.h, () -> { });
                level.addBorder(door);
                level.addItemKeyLock(door, ik.keyId);
            }
        }

        for (Point p : bp.checkpoints) {
            level.queueAddVisualObject(new Checkpoint(p.x, p.y, () -> {
                if (player != null) player.saveCheckpoint();
            }));
        }
        // auto-place key pickups for any ItemKeyLocks if not specified
        java.util.Map<String, Integer> keyToRoom = new java.util.HashMap<>();
        for (LevelBlueprint.LockPlacement lp : bp.lockPlacements) {
            if (lp != null && lp.lock instanceof Levels.Generation.LockSpec.ItemKeyLock) {
                String kid = ((Levels.Generation.LockSpec.ItemKeyLock) lp.lock).keyId;
                int roomIdx = Math.max(0, lp.roomIndex - 1);
                keyToRoom.put(kid, Math.min(roomIdx, Math.max(0, bp.rooms.size() - 1)));
            }
        }
        for (java.util.Map.Entry<String, Integer> e : keyToRoom.entrySet()) {
            String kid = e.getKey();
            boolean already = false;
            for (LevelBlueprint.KeyPickupSpec kp : bp.keyPickups) if (kid.equals(kp.keyId)) { already = true; break; }
            if (!already && !bp.rooms.isEmpty() && e.getValue() >= 0 && e.getValue() < bp.rooms.size()) {
                Rect r = bp.rooms.get(e.getValue());
                bp.keyPickups.add(new LevelBlueprint.KeyPickupSpec(r.centerX() - 10, r.centerY() - 10, kid));
            } else if (!already && !bp.rooms.isEmpty()) {
                Rect r0 = bp.rooms.get(0);
                bp.keyPickups.add(new LevelBlueprint.KeyPickupSpec(r0.centerX() - 10, r0.centerY() - 10, kid));
            }
        }
        for (LevelBlueprint.PuzzleButtonSpec pb : bp.puzzleButtons) {
            level.queueAddVisualObject(new PuzzleButton(pb.x, pb.y, pb.puzzleId, level::setPuzzleSolved));
        }
        for (LevelBlueprint.MazeExitPortalSpec me : bp.mazeExits) {
            level.queueAddVisualObject(new MazeExitPortal(me.x, me.y, me.w, me.h, me.puzzleId, level::setPuzzleSolved));
        }
        for (LevelBlueprint.KeyPickupSpec kp : bp.keyPickups) {
            if (player != null) {
                level.queueAddVisualObject(new KeyPickup(kp.x, kp.y, kp.keyId, player::grantKey));
            }
        }

        for (LevelBlueprint.ChestSpec c : bp.chests) {
            LootTable loot = lootForTier(c.lootTier);
            Chest chest = new Chest(c.x, c.y, loot, c.rollCount, null);
            chest.setOnOpenAt((items, chestPosition) -> {
                int lootX = chestPosition.x - 20;
                int lootY = chestPosition.y + 44;
                for (int i = 0; i < items.size(); i++) {
                    Item it = items.get(i);
                    level.queueAddVisualObject(new DroppedItem(lootX + (i * 36), lootY, it, level::collectItem));
                }
            });
            level.queueAddVisualObject(chest);
        }

        if (bp.boss != null) {
            BossEnemy boss = new BossEnemy(bp.boss.x, bp.boss.y);
            int bossHealth = bp.boss.health;
            try {
                Levels.Generation.BossPool pool = Levels.Generation.BossPool.defaultPool();
                Levels.Generation.BossDefinition bd = pool.pick(ctx.random);
                bossHealth = bd.baseHealth + Math.max(0, ctx.stageCount - 8) * bd.healthPerStage + ctx.stage * 10;
            } catch (Exception ignored) {
                // keep bp.boss.health
            }
            boss.setHealth(bossHealth);
            boss.setWeapon(new RangedWeapon(16 + ctx.difficulty(), 70, 500, 7.0));
            chainOnDeath(level, boss, null, ctx.random);
            level.addEnemy(boss);
            level.objective = lvl -> boss.isDead();
        } else {
            level.objective = lvl -> lvl.getEnemies().isEmpty();
        }

        level.setRoomRegions(bp.roomRegions);
        level.setHazardZones(bp.hazardZones);
        if (player != null) {
            player.setX(bp.playerSpawn.x);
            player.setY(bp.playerSpawn.y);
        }
    }

    private static void attachDefaultDrops(BaseLevel level, List<Enemy> enemies, Random random) {
        for (Enemy enemy : enemies) {
            chainOnDeath(level, enemy, null, random);
        }
    }

    private static void chainOnDeath(BaseLevel level, Enemy enemy, Runnable extra, Random random) {
        enemy.setOnDeath(() -> {
            if (random.nextInt(100) < 40) {
                level.queueAddVisualObject(new DroppedItem(enemy.getX(), enemy.getY(), new HealthPotion(), level::collectItem));
            }
            if (extra != null) extra.run();
        });
    }

    private static Enemy createEnemy(LevelBlueprint.EnemySpawn spec, GenerationContext ctx) {
        int hp = 50 + ctx.difficulty() * 10;
        int dmg = 8 + ctx.difficulty() * 2;
        switch (spec.kind) {
            case RANGED:
                MeleeEnemy ranged = new MeleeEnemy(spec.x, spec.y, 40 + ctx.difficulty() * 7, 8 + ctx.difficulty(), 450, 1, 300);
                ranged.setWeapon(new RangedWeapon(8 + ctx.difficulty(), 55, 420, 6.5));
                return ranged;
            case BERSERKER:
                MeleeEnemy berserker = new MeleeEnemy(spec.x, spec.y, 45 + ctx.difficulty() * 8, 10 + ctx.difficulty() * 2, 350, 2, 40);
                berserker.setWeapon(new BerserkerWeapon());
                return berserker;
            case TANK:
                MeleeEnemy tank = new MeleeEnemy(spec.x, spec.y, 80 + ctx.difficulty() * 12, 10 + ctx.difficulty(), 320, 1, 42);
                tank.setWeapon(new TankEnemySword());
                return tank;
            case BOSS:
                BossEnemy boss = new BossEnemy(spec.x, spec.y);
                boss.setWeapon(new RangedWeapon(14, 70, 500, 7.0));
                return boss;
            case MELEE:
            default:
                MeleeEnemy melee = new MeleeEnemy(spec.x, spec.y, hp, dmg, 350, 2, 42);
                melee.setWeapon(new TankEnemySword());
                return melee;
        }
    }

    private static LootTable lootForTier(int tier) {
        if (tier >= 5) {
            return LootTable.of(
                    LootTable.item(3, Hammer::new), LootTable.item(3, Dagger::new), LootTable.item(3, BasicWand::new),
                    LootTable.item(3, KnightChestplate::new), LootTable.item(3, RogueTunic::new), LootTable.item(3, WizardRobe::new),
                    LootTable.item(2, KnightHelmet::new), LootTable.item(2, RogueHood::new), LootTable.item(2, WizardHat::new),
                    LootTable.item(4, HealthPotion::new));
        }
        return LootTable.of(
                LootTable.item(2, Hammer::new), LootTable.item(2, Dagger::new), LootTable.item(2, BasicWand::new),
                LootTable.item(2, KnightChestplate::new), LootTable.item(2, RogueTunic::new), LootTable.item(2, WizardRobe::new),
                LootTable.item(2, KnightHelmet::new), LootTable.item(2, RogueHood::new), LootTable.item(2, WizardHat::new),
                LootTable.item(5, HealthPotion::new));
    }
}
