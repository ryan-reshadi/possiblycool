package Objects.PlayerEquipment.Spells;

import Objects.EnemyClasses.Enemy;

public class BurnPassive extends PassiveSpell {

    @Override
    public void applyOnHit(Enemy enemy, int currentTick) {
        if (enemy != null) {
            enemy.applyBurn(currentTick);
        }
    }

    @Override
    public String getName() {
        return "Burn";
    }

    @Override
    public String getDescription() {
        return "Sets enemy on fire for 3 seconds, burning them over time.";
    }
}
