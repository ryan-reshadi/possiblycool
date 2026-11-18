package Levels.Generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class BossPool {
    private final List<BossDefinition> bosses;

    public BossPool(List<BossDefinition> bosses) {
        if (bosses == null || bosses.isEmpty()) {
            this.bosses = List.of(new BossDefinition("default", 260, 10));
        } else {
            this.bosses = List.copyOf(bosses);
        }
    }

    public static BossPool defaultPool() {
        return new BossPool(List.of(
                new BossDefinition("default", 260, 10)
                // Future: add tougher bosses here
        ));
    }

    public BossDefinition pick(Random rng) {
        if (bosses.size() == 1) return bosses.get(0);
        return bosses.get(rng.nextInt(bosses.size()));
    }

    public List<BossDefinition> all() {
        return bosses;
    }
}
