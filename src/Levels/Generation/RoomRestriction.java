package Levels.Generation;

/** Runtime/player restrictions applied in certain rooms/regions. */
public final class RoomRestriction {
    public final boolean disableWeapons;
    public final boolean disableAbilities; // Q/E/F
    public final boolean disableUltimates; // R

    public static final RoomRestriction NONE = new RoomRestriction(false, false, false);

    public static final RoomRestriction NO_COMBAT_ACTIONS = new RoomRestriction(true, true, true);

    public RoomRestriction(boolean disableWeapons, boolean disableAbilities, boolean disableUltimates) {
        this.disableWeapons = disableWeapons;
        this.disableAbilities = disableAbilities;
        this.disableUltimates = disableUltimates;
    }

    public boolean isAnyRestricted() {
        return disableWeapons || disableAbilities || disableUltimates;
    }
}
