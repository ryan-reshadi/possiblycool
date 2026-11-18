package Levels.Generation;

import java.util.EnumSet;
import java.util.Set;

public final class RoomTags {
    private final Set<RoomTag> tags = EnumSet.noneOf(RoomTag.class);

    public enum RoomTag {
        RESTRICTED_ABILITIES_ULTS,
        RESTRICTED_WEAPONS,
        RESTRICTED_ALL_COMBAT_ACTIONS,
        NO_ENEMIES_DEFAULT,
        REWARD,
        HAZARD,
        PUZZLE,
        SHOP,
        BOSS_ONLY
    }

    public RoomTags add(RoomTag t) {
        if (t != null) tags.add(t);
        return this;
    }

    public boolean has(RoomTag t) {
        return tags.contains(t);
    }

    public Set<RoomTag> asSet() {
        return EnumSet.copyOf(tags.isEmpty() ? EnumSet.noneOf(RoomTag.class) : tags);
    }

    public RoomRestriction toRestriction() {
        if (has(RoomTag.RESTRICTED_ALL_COMBAT_ACTIONS)) {
            return RoomRestriction.NO_COMBAT_ACTIONS;
        }
        boolean weapons = has(RoomTag.RESTRICTED_WEAPONS);
        boolean abilUlt = has(RoomTag.RESTRICTED_ABILITIES_ULTS);
        return new RoomRestriction(weapons, abilUlt, abilUlt);
    }
}
