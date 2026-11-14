package Objects.PlayerEquipment;

import Objects.PlayerClasses.Player;
import Levels.BaseLevel;
import java.awt.Color;

/**
 * Abstract base class for active abilities (Q/E/F slots).
 * Abilities use cooldowns only — no mana.
 */
public abstract class Ability {

    protected int cooldownTicks;
    protected int lastUsedTick = -1; // -1 = never used
    protected int targetX;
    protected int targetY;
    protected Color iconColor = Color.MAGENTA;
    protected String spritePath = "images/drops/scroll.png";

    public String getSpritePath() {
        return spritePath;
    }

    public void setSpritePath(String spritePath) {
        this.spritePath = spritePath;
    }

    public Ability(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public void setTarget(int x, int y) {
        this.targetX = x;
        this.targetY = y;
    }

    public int getTargetX() {
        return targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public Color getIconColor() {
        return iconColor;
    }

    public void setIconColor(Color color) {
        this.iconColor = color;
    }

    /**
     * Returns true if the ability is ready to use.
     * Ready if never used OR enough ticks have passed since last use.
     */
    public boolean isReady(int currentTick) {
        return lastUsedTick == -1 || currentTick >= lastUsedTick + cooldownTicks;
    }

    /**
     * Returns cooldown progress: 0.0 = on cooldown, 1.0 = ready. Clamped to [0,1].
     */
    public double getCooldownProgress(int currentTick) {
        if (lastUsedTick == -1) return 1.0;
        double progress = (double)(currentTick - lastUsedTick) / cooldownTicks;
        return Math.max(0.0, Math.min(1.0, progress));
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public int getLastUsedTick() {
        return lastUsedTick;
    }

    public abstract void activate(Player player, BaseLevel level, int currentTick);

    public abstract String getName();

    public abstract String getDescription();
}
