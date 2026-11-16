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
    public final List<Rect> rooms = new ArrayList<>();
    public final List<EnemySpawn> enemies = new ArrayList<>();
    public final List<ChestSpec> chests = new ArrayList<>();
    public final List<Point> checkpoints = new ArrayList<>();
    public final List<GateSpec> gates = new ArrayList<>();
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

    public void addRoom(Rect room) { rooms.add(room); }
}
