package Objects.PlayerEquipment.Spells;

import Objects.Projectile;
import java.awt.Color;

/**
 * Active spell: Ice Bolt
 * Damage: 15, Speed: 6.0, Color: CYAN
 * Slow effect is handled by the SlowPassive passive spell, not here.
 */
public class IceBoltSpell extends ActiveSpell {

    public IceBoltSpell() {
        super("Ice Bolt", 15, 6.0);
    }

    @Override
    public Projectile createProjectile(int startX, int startY, double angle, Runnable onHit) {
        return new Projectile(startX, startY, angle, projectileSpeed, damage, Color.CYAN, onHit);
    }

    @Override
    public String getName() {
        return name;
    }
}
