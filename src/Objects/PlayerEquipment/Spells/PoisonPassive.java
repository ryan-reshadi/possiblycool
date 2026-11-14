package Objects.PlayerEquipment.Spells;

import Objects.EnemyClasses.Enemy;

public class PoisonPassive extends PassiveSpell {

    @Override
    public void applyOnHit(Enemy enemy, int currentTick) {
        if (enemy != null) {
            enemy.applyPoison(currentTick);
        }
    }

    @Override
    public String getName() {
        return "Poison";
    }

    @Override
    public String getDescription() {
        return "Poisons enemy for 5 seconds, dealing damage over time.";
    }
}
