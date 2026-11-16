package Objects.PlayerEquipment;

import GameRun.InputFrame;
import Levels.BaseLevel;
import Objects.PlayerClasses.Player;

import java.awt.Color;
import java.awt.Graphics2D;

/** Base class for class-specific ultimates (charge-activated, may be roaming/empowered). */
public abstract class Ultimate {

    protected boolean active;
    protected double activeRemainingTicks;
    protected double activeMaxTicks;

    public abstract String getId();
    public abstract String getName();
    public abstract String getDescription();
    public abstract Color getIconColor();

    /** Attempt to activate from a ready (fully charged) inactive state. */
    public boolean tryActivate(Player player, BaseLevel level, int currentTick, int targetX, int targetY) {
        if (active) return false;
        if (!canActivate(player, level, currentTick)) return false;
        activate(player, level, currentTick, targetX, targetY);
        return true;
    }

    protected boolean canActivate(Player player, BaseLevel level, int currentTick) {
        return true;
    }

    protected abstract void activate(Player player, BaseLevel level, int currentTick, int targetX, int targetY);

    /** Called on R while active. Default: no-op (not cancellable). */
    public boolean tryRetrigger(Player player, BaseLevel level, int currentTick, int targetX, int targetY) {
        return false;
    }

    public void tick(Player player, BaseLevel level, int currentTick, InputFrame input) {
        if (!active) return;
        if (activeMaxTicks > 0) {
            activeRemainingTicks -= 1.0;
            if (activeRemainingTicks <= 0) {
                activeRemainingTicks = 0;
                end(player, level, currentTick, false);
            }
        }
    }

    /** End the active state. */
    public void end(Player player, BaseLevel level, int currentTick, boolean cancelled) {
        if (!active) return;
        active = false;
        activeRemainingTicks = 0;
        onEnd(player, level, currentTick, cancelled);
    }

    protected void onEnd(Player player, BaseLevel level, int currentTick, boolean cancelled) {
    }

    public boolean isActive() {
        return active;
    }

    /** While true, the player cannot gain ultimate charge (passive or from damage). */
    public boolean blocksChargeGain() {
        return active;
    }

    /** Normal abilities remain available unless explicitly overridden. */
    public boolean allowsNormalAbilities() {
        return true;
    }

    /** When true, normal weapon attacks are replaced while this ultimate is active (subject to exceptions like consumables). */
    public boolean overridesWeaponAttack() {
        return false;
    }

    /** Handle a weapon-attack press/click while active and overriding. Return true if consumed. */
    public boolean handleWeaponPress(Player player, BaseLevel level, int currentTick, int clickXDown, int clickYDown) {
        return false;
    }

    /** Handle continuous held-attack input while active and overriding. */
    public void handleHeldAttack(Player player, BaseLevel level, int currentTick, InputFrame input) {
    }

    /** Notify that the player performed a normal attack (for chunk-drain roaming ultimates). */
    public void onPlayerAttack(Player player, BaseLevel level, int currentTick) {
    }

    public double getDamageMultiplier() { return 1.0; }
    public int getAttackRangeBonus() { return 0; }
    public double getDamageReductionBonus() { return 0.0; }

    /** For UI: 0..1 progress. When active roaming, represents remaining active time; otherwise charge progress supplied by player. */
    public double getActiveProgress() {
        if (!active || activeMaxTicks <= 0) return 0.0;
        return Math.max(0.0, Math.min(1.0, activeRemainingTicks / activeMaxTicks));
    }

    public boolean isRoamingActive() {
        return active && activeMaxTicks > 0;
    }

    public void render(Graphics2D g, int tickCount) {
    }
}
