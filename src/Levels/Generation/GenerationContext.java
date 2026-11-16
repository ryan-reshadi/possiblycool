package Levels.Generation;

import java.util.Objects;
import java.util.Random;

/** Deterministic inputs for generating a run level. */
public final class GenerationContext {
    public final String nodeId;
    public final int stage;
    public final int stageCount;
    public final long seed;
    public final Random random;
    public final boolean boss;

    public GenerationContext(String nodeId, int stage, int stageCount, long seed, boolean boss) {
        this.nodeId = Objects.toString(nodeId, "node");
        this.stage = Math.max(1, stage);
        this.stageCount = Math.max(1, stageCount);
        this.seed = seed;
        this.boss = boss;
        this.random = new Random(mix(seed, this.nodeId, this.stage, boss));
    }

    public static GenerationContext combat(String nodeId, int stage, int stageCount, long seed) {
        return new GenerationContext(nodeId, stage, stageCount, seed, false);
    }

    public static GenerationContext boss(String nodeId, int stages, long seed) {
        return new GenerationContext(nodeId, Math.max(1, stages), stages, seed, true);
    }

    public int difficulty() {
        return stage + (boss ? 2 : 0);
    }

    private static long mix(long seed, String id, int stage, boolean boss) {
        long h = seed ^ ((long) stage * 0x9E3779B97F4A7C15L);
        h ^= id.hashCode() * 0xBF58476D1CE4E5B9L;
        h ^= boss ? 0xD6E8FEB86659FD93L : 0L;
        h ^= (h >>> 33);
        h *= 0xff51afd7ed558ccdL;
        h ^= (h >>> 33);
        return h;
    }
}
