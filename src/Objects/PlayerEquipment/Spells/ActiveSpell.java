package Objects.PlayerEquipment.Spells;

import Objects.Projectile;

/**
 * Abstract base for active spell types fired by a Wand.
 * Each wand randomly selects one ActiveSpell on construction.
 */
public abstract class ActiveSpell {

    protected String name;
    protected int damage;
    protected double projectileSpeed;

    public ActiveSpell(String name, int damage, double projectileSpeed) {
        this.name = name;
        this.damage = damage;
        this.projectileSpeed = projectileSpeed;
    }

    /**
     * Create a Projectile aimed at the given angle from (startX, startY).
     * onHit is called when the projectile hits an enemy (before it removes itself).
     */
    public abstract Projectile createProjectile(int startX, int startY, double angle, Runnable onHit);

    public abstract String getName();

    public int getDamage() { return damage; }
    public double getProjectileSpeed() { return projectileSpeed; }
}
