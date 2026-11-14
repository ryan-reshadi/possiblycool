package Objects.PlayerEquipment.Spells;

import Objects.EnemyClasses.Enemy;

/**
 * Abstract base for on-hit passive effects applied when a wand's spell bolt hits an enemy.
 * Each wand randomly selects two PassiveSpells on construction.
 */
public abstract class PassiveSpell {

    /**
     * Called when a projectile hits an enemy. Apply the passive effect here.
     */
    public abstract void applyOnHit(Enemy enemy, int currentTick);

    public abstract String getName();

    public abstract String getDescription();
}
