package Levels;

import Objects.EnemyClasses.Enemy;
import java.util.Objects;

public final class DefeatBossObjective implements LevelObjective {
    private final Enemy boss;

    public DefeatBossObjective(Enemy boss) {
        this.boss = Objects.requireNonNull(boss, "boss");
    }

    @Override
    public boolean isComplete(BaseLevel level) {
        return boss.isDead();
    }
}
