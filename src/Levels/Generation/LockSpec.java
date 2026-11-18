package Levels.Generation;

import java.util.Collections;
import java.util.List;

/** Extensible lock model for doors/barriers. */
public abstract class LockSpec {
    public enum Kind { KILL_GUARDS, ITEM_KEY, PUZZLE }

    public final Kind kind;

    protected LockSpec(Kind kind) {
        this.kind = kind;
    }

    public static final class KillGuardLock extends LockSpec {
        /** Enemy indices in LevelBlueprint.enemies that must be dead. */
        public final List<Integer> guardEnemyIndices;
        public final int roomIndex; // room where guards live (informational)

        public KillGuardLock(List<Integer> guardEnemyIndices, int roomIndex) {
            super(Kind.KILL_GUARDS);
            this.guardEnemyIndices = guardEnemyIndices == null ? Collections.emptyList() : List.copyOf(guardEnemyIndices);
            this.roomIndex = roomIndex;
        }
    }

    public static final class ItemKeyLock extends LockSpec {
        public final String keyId; // e.g. "bronze_key"

        public ItemKeyLock(String keyId) {
            super(Kind.ITEM_KEY);
            this.keyId = keyId == null ? "" : keyId;
        }
    }

    public enum PuzzleType { BUTTONS, MAZE, GENERIC }

    public static final class PuzzleLock extends LockSpec {
        public final PuzzleType puzzleType;
        public final String puzzleId; // optional identifier

        public PuzzleLock(PuzzleType puzzleType, String puzzleId) {
            super(Kind.PUZZLE);
            this.puzzleType = puzzleType == null ? PuzzleType.GENERIC : puzzleType;
            this.puzzleId = puzzleId == null ? "" : puzzleId;
        }
    }
}
