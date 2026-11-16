package Objects.PlayerEquipment.Ultimates;

import GameRun.InputFrame;
import Levels.BaseLevel;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Ultimate;

import java.awt.Color;

public class BattleCryUltimate extends Ultimate {

    public static final int DURATION_TICKS = 360; // 6 seconds
    public static final double DAMAGE_REDUCTION_BONUS = 0.25;
    public static final double DAMAGE_MULTIPLIER = 1.5;
    public static final int ATTACK_RANGE_BONUS = 40;
    public static final double ATTACK_CHUNK_TICKS = 30.0; // extra drain per attack

    @Override
    public String getId() { return "battle-cry"; }

    @Override
    public String getName() { return "Battle Cry"; }

    @Override
    public String getDescription() {
        return "Enter an empowered state: increased damage reduction, range, and damage. Retrigger [R] to end early.";
    }

    @Override
    public Color getIconColor() { return new Color(255, 120, 60); }

    @Override
    protected void activate(Player player, BaseLevel level, int currentTick, int targetX, int targetY) {
        active = true;
        activeMaxTicks = DURATION_TICKS;
        activeRemainingTicks = DURATION_TICKS;
        player.recalcArmorReduction();
    }

    @Override
    public boolean tryRetrigger(Player player, BaseLevel level, int currentTick, int targetX, int targetY) {
        if (!active) return false;
        end(player, level, currentTick, true);
        return true;
    }

    @Override
    protected void onEnd(Player player, BaseLevel level, int currentTick, boolean cancelled) {
        activeMaxTicks = 0;
        activeRemainingTicks = 0;
        player.recalcArmorReduction();
    }

    @Override
    public void onPlayerAttack(Player player, BaseLevel level, int currentTick) {
        if (!active) return;
        activeRemainingTicks = Math.max(0, activeRemainingTicks - ATTACK_CHUNK_TICKS);
        if (activeRemainingTicks <= 0) {
            end(player, level, currentTick, false);
        }
    }

    @Override
    public double getDamageMultiplier() { return active ? DAMAGE_MULTIPLIER : 1.0; }

    @Override
    public int getAttackRangeBonus() { return active ? ATTACK_RANGE_BONUS : 0; }

    @Override
    public double getDamageReductionBonus() { return active ? DAMAGE_REDUCTION_BONUS : 0.0; }
}
