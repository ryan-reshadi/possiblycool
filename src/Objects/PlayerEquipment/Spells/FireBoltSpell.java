package Objects.PlayerEquipment.Spells;

import Objects.Projectile;
import java.awt.Color;

/**
 * Active spell: Fire Bolt
 * Damage: 25, Speed: 8.0, Color: ORANGE
 */
public class FireBoltSpell extends ActiveSpell {

    public FireBoltSpell() {
        super("Fire Bolt", 25, 8.0);
    }

    @Override
    public Projectile createProjectile(int startX, int startY, double angle, Runnable onHit) {
        return new Projectile(startX, startY, angle, projectileSpeed, damage, Color.ORANGE, onHit);
    }

    @Override
    public String getName() {
        return name;
    }
}
