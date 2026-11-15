package Objects.EnemyClasses;

/** Tank sword variant with more telegraph time before each combo hit. */
public class TankEnemySword extends TankWeapon {
    private static final int[] ANIMATION_TICKS = {34, 40, 47};
    private static final int[] DAMAGE_DELAY_TICKS = {22, 26, 30};

    @Override
    protected int getAnimationTicks(int move) {
        return ANIMATION_TICKS[move];
    }

    @Override
    protected int getDamageDelayTicks(int move) {
        return DAMAGE_DELAY_TICKS[move];
    }
}
