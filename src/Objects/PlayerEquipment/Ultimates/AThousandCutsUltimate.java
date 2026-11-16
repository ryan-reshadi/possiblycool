package Objects.PlayerEquipment.Ultimates;

import Levels.BaseLevel;
import Objects.Effects.ThousandCutsEffect;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Ultimate;

import java.awt.Color;

public class AThousandCutsUltimate extends Ultimate {

    public static final int PATH_LENGTH = 450;
    public static final int PATH_WIDTH = 70;
    public static final int TELEGRAPH_TICKS = 30; // ~0.5s
    public static final int BASE_DAMAGE = 70;
    public static final double MAX_HEALTH_PERCENT = 0.12;

    @Override
    public String getId() { return "a-thousand-cuts"; }

    @Override
    public String getName() { return "A Thousand Cuts"; }

    @Override
    public String getDescription() {
        return "Dash along a telegraphed path, slashing all enemies for heavy damage plus a portion of their max health.";
    }

    @Override
    public Color getIconColor() { return new Color(230, 230, 240); }

    @Override
    protected void activate(Player player, BaseLevel level, int currentTick, int targetX, int targetY) {
        // Instant cast that spawns a resolving effect; no roaming active state remains.
        active = false;
        activeMaxTicks = 0;
        activeRemainingTicks = 0;
        ThousandCutsEffect effect = new ThousandCutsEffect(player, level, currentTick, targetX, targetY,
                PATH_LENGTH, PATH_WIDTH, TELEGRAPH_TICKS, BASE_DAMAGE, MAX_HEALTH_PERCENT);
        level.queueAddVisualObject(effect);
    }
}
