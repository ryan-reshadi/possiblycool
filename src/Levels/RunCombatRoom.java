package Levels;

import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/** Procedurally generated multi-room combat level for route ROOM nodes. */
public final class RunCombatRoom extends BaseLevel {
    public RunCombatRoom(Player player, int stage, long seed) {
        super(new Color(22, 24, 30), new ArrayList<VisualObject>(List.of(player)));
        RunGeneratedBuilder.buildCombat(this, player, stage, seed);
    }
}
