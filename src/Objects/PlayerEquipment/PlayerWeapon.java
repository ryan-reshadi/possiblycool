package Objects.PlayerEquipment;

import java.util.Random;

/** Shared progression stats for player-owned weapons. */
public abstract class PlayerWeapon extends Item {
    private static final double MIN_DAMAGE_ROLL = 0.9;
    private static final double MAX_DAMAGE_ROLL = 1.1;
    private static final Random RANDOM = new Random();

    private int level = 1;
    private double damageRoll;

    protected PlayerWeapon() {
        this(MIN_DAMAGE_ROLL + RANDOM.nextDouble() * (MAX_DAMAGE_ROLL - MIN_DAMAGE_ROLL));
    }

    protected PlayerWeapon(double damageRoll) {
        if (Double.isNaN(damageRoll) || Double.isInfinite(damageRoll)
                || damageRoll < MIN_DAMAGE_ROLL || damageRoll > MAX_DAMAGE_ROLL) {
            throw new IllegalArgumentException("Weapon damage roll must be between 0.9 and 1.1.");
        }
        this.damageRoll = damageRoll;
    }

    /** Rebuilds a saved weapon with its original random damage roll. */
    public final void restoreProgression(double damageRoll, int level) {
        if (Double.isNaN(damageRoll) || Double.isInfinite(damageRoll)
                || damageRoll < MIN_DAMAGE_ROLL || damageRoll > MAX_DAMAGE_ROLL) {
            throw new IllegalArgumentException("Weapon damage roll must be between 0.9 and 1.1.");
        }
        this.damageRoll = damageRoll;
        setLevel(level);
    }

    public final int getLevel() {
        return level;
    }

    public final void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public final double getDamageRollMultiplier() {
        return damageRoll;
    }

    public final int getScaledDamage(int baseDamage) {
        int base = Math.max(1, baseDamage);
        int minimumDamage = Math.max(1, (int) Math.round(base * MIN_DAMAGE_ROLL));
        int maximumDamage = Math.max(minimumDamage, (int) Math.round(base * MAX_DAMAGE_ROLL));
        int damageVariation = maximumDamage - minimumDamage;
        int rolledDamage = minimumDamage
                + (int) Math.round((damageRoll - MIN_DAMAGE_ROLL)
                        / (MAX_DAMAGE_ROLL - MIN_DAMAGE_ROLL) * damageVariation);
        long damageBandWidth = (long) damageVariation + 1;
        long levelDamage = rolledDamage + ((long) level - 1) * damageBandWidth;
        return (int) Math.min(Integer.MAX_VALUE, levelDamage);
    }

    static int rollLevelNear(int referenceLevel, Random random) {
        if (random == null) throw new IllegalArgumentException("Random source cannot be null.");
        int roll = random.nextInt(100);
        int offset = roll < 15 ? -1 : roll < 50 ? 0 : 1;
        return Math.max(1, referenceLevel + offset);
    }
}
