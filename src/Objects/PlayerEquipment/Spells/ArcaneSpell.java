package Objects.PlayerEquipment.Spells;

import Objects.Projectile;
import java.awt.Color;

/**
 * Active spell: Arcane
 * Damage: 35, Speed: 10.0, Color: MAGENTA
 */
public class ArcaneSpell extends ActiveSpell {

    public ArcaneSpell() {
        super("Arcane", 35, 10.0);
    }

    @Override
    public Projectile createProjectile(int startX, int startY, double angle, Runnable onHit) {
        return new Projectile(startX, startY, angle, projectileSpeed, damage, Color.MAGENTA, onHit);
    }

    @Override
    public String getName() {
        return name;
    }
}
