package Levels;

import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/** Procedurally generated boss level for route BOSS nodes. */
public final class RunBossRoom extends BaseLevel {
    public RunBossRoom(Player player, int stages) {
        this(player, stages, (((long) stages) * 0x9E3779B97F4A7C15L));
    }

    public RunBossRoom(Player player, int stages, long seed) {
        super(new Color(30, 14, 18), new ArrayList<VisualObject>(List.of(player)));
        RunGeneratedBuilder.buildBoss(this, player, stages, seed);
    }
}
