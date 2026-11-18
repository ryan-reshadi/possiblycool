package Levels.Generation;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/** Pure-data description of a generated level before runtime objects are created. */
public final class LevelBlueprint {
    public int width;
    public int height;
    public Point playerSpawn = new Point(300, 500);
    public final List<Rect> walls = new ArrayList<>();
    public final List<Rect> corridors = new ArrayList<>();
    public final List<Rect> rooms = new ArrayList<>();
    public final List<EnemySpawn> enemies = new ArrayList<>();
    public final List<ChestSpec> chests = new ArrayList<>();
    public final List<Point> checkpoints = new ArrayList<>();
    public final List<GateSpec> gates = new ArrayList<>();
    /** Additive: generalized lock placements (item/puzzle/kill-guard). */
    public final List<LockPlacement> lockPlacements = new ArrayList<>();
    /** Additive: room regions with restrictions (abilities/weapons). */
    public final List<RoomRegionSpec> roomRegions = new ArrayList<>();
    /** Additive: hazard zones with effects. */
    public final List<HazardZoneSpec> hazardZones = new ArrayList<>();
    /** Additive: puzzle button placements (minimal). */
    public final List<PuzzleButtonSpec> puzzleButtons = new ArrayList<>();
    public final List<KeyPickupSpec> keyPickups = new ArrayList<>();
    public final List<MazeExitPortalSpec> mazeExits = new ArrayList<>();
    public BossSpec boss;

    public enum EnemyKind { MELEE, BERSERKER, RANGED, TANK, BOSS }

    public static final class EnemySpawn {
        public final EnemyKind kind;
        public final int x;
        public final int y;
        public final int roomIndex;
        public EnemySpawn(EnemyKind kind, int x, int y, int roomIndex) {
            this.kind = kind; this.x = x; this.y = y; this.roomIndex = roomIndex;
        }
    }

    public static final class ChestSpec {
        public final int x;
        public final int y;
        public final int rollCount;
        public final int lootTier;
        public ChestSpec(int x, int y, int rollCount, int lootTier) {
            this.x = x; this.y = y; this.rollCount = rollCount; this.lootTier = lootTier;
        }
    }

    public static final class GateSpec {
        /** Door rect blocks passage while locked. */
        public final Rect door;
        /** Indices in enemies list that must be dead to unlock. */
        public final List<Integer> guardEnemyIndices = new ArrayList<>();
        public final int roomIndex;
        public GateSpec(Rect door, int roomIndex) { this.door = door; this.roomIndex = roomIndex; }
    }

    public static final class BossSpec {
        public final int x;
        public final int y;
        public final int health;
        public BossSpec(int x, int y, int health) { this.x = x; this.y = y; this.health = health; }
    }

    public static final class RoomRegionSpec {
        public final Rect rect;
        public final RoomRestriction restriction;
        public final int roomIndex;
        public RoomRegionSpec(Rect rect, RoomRestriction restriction, int roomIndex) {
            this.rect = rect;
            this.restriction = restriction == null ? RoomRestriction.NONE : restriction;
            this.roomIndex = roomIndex;
        }
    }

    public static final class LockPlacement {
        public final Rect doorRect;
        public final LockSpec lock;
        public final int roomIndex;
        public LockPlacement(Rect doorRect, LockSpec lock, int roomIndex) {
            this.doorRect = doorRect;
            this.lock = lock;
            this.roomIndex = roomIndex;
        }
    }

    public static final class PuzzleButtonSpec {
        public final int x;
        public final int y;
        public final String puzzleId;
        public PuzzleButtonSpec(int x, int y, String puzzleId) {
            this.x = x; this.y = y; this.puzzleId = puzzleId == null ? "" : puzzleId;
        }
    }

    public static final class KeyPickupSpec {
        public final int x;
        public final int y;
        public final String keyId;
        public KeyPickupSpec(int x, int y, String keyId) {
            this.x = x; this.y = y; this.keyId = keyId == null ? "" : keyId;
        }
    }

    public static final class MazeExitPortalSpec {
        public final int x, y, w, h;
        public final String puzzleId;
        public MazeExitPortalSpec(int x, int y, int w, int h, String puzzleId) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.puzzleId = puzzleId;
        }
    }

    public enum HazardEffectType { DAMAGE_OVER_TIME, SLOW }

    public static final class HazardZoneSpec {
        public final Rect rect;
        public final java.util.List<HazardEffectType> effects;
        public final boolean disableWeapons;
        public final boolean disableAbilitiesUlt;
        public HazardZoneSpec(Rect rect, java.util.List<HazardEffectType> effects, boolean disableWeapons, boolean disableAbilitiesUlt) {
            this.rect = rect;
            this.effects = effects == null ? java.util.List.of() : java.util.List.copyOf(effects);
            this.disableWeapons = disableWeapons;
            this.disableAbilitiesUlt = disableAbilitiesUlt;
        }
    }

    public void addRoom(Rect room) { rooms.add(room); }
}
