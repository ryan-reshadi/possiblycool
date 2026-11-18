package Levels.Generation;

import java.util.EnumMap;
import java.util.Map;

public final class RoomTemplateRegistry {
    private final Map<RoomTypeId, RoomTypeTemplate> templates = new EnumMap<>(RoomTypeId.class);

    public RoomTemplateRegistry() {
        registerDefaults();
    }

    private void registerDefaults() {
        register(new RoomTypeTemplate(RoomTypeId.START_ROOM, 400, 500, 400, 600, 420, 500, "NORMAL", RoomRestriction.NONE));
        register(new RoomTypeTemplate(RoomTypeId.STANDARD_COMBAT, 480, 600, 380, 500, 520, 420, "NORMAL", RoomRestriction.NONE));
        register(new RoomTypeTemplate(RoomTypeId.SIDE_REWARD, 300, 400, 260, 360, 340, 300, "NARROW", RoomRestriction.NONE));
        register(new RoomTypeTemplate(RoomTypeId.BOSS_START, 400, 500, 400, 600, 420, 500, "NORMAL", RoomRestriction.NONE));
        register(new RoomTypeTemplate(RoomTypeId.BOSS_ANTECHAMBER, 480, 600, 600, 800, 520, 700, "NORMAL", RoomRestriction.NONE));
        register(new RoomTypeTemplate(RoomTypeId.BOSS_ARENA, 800, 1000, 800, 1000, 900, 900, "WIDE", RoomRestriction.NONE));
        register(new RoomTypeTemplate(RoomTypeId.PUZZLE_BUTTONS, 400, 520, 400, 620, 420, 600, "NORMAL", RoomRestriction.NO_COMBAT_ACTIONS));
        register(new RoomTypeTemplate(RoomTypeId.PUZZLE_MAZE, 500, 700, 500, 700, 600, 600, "NORMAL", RoomRestriction.NO_COMBAT_ACTIONS));
        register(new RoomTypeTemplate(RoomTypeId.HAZARD_ROOM, 400, 600, 400, 600, 500, 500, "NORMAL", RoomRestriction.NONE));
        register(new RoomTypeTemplate(RoomTypeId.SHOP_ROOM, 400, 500, 400, 500, 450, 450, "NORMAL", RoomRestriction.NONE));
    }

    public void register(RoomTypeTemplate t) {
        if (t != null && t.id != null) templates.put(t.id, t);
    }

    public RoomTypeTemplate get(RoomTypeId id) {
        return templates.get(id);
    }
}
