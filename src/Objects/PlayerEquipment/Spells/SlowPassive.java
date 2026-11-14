package Objects.PlayerEquipment.Spells;

import Objects.EnemyClasses.Enemy;

public class SlowPassive extends PassiveSpell {

    @Override
    public void applyOnHit(Enemy enemy, int currentTick) {
        if (enemy != null) {
            enemy.applySlow(currentTick);
        }
    }

    @Override
    public String getName() {
        return "Slow";
    }

    @Override
    public String getDescription() {
        return "Slows enemy movement by 50% for 2 seconds.";
    }
}
