package Objects.PlayerEquipment;

import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Abstract base for consumable items (potions, etc.).
 */
public abstract class Consumable extends Item {

    public static final int MAX_STACK_SIZE = 64;

    private final int maxUsesPerItem;
    protected int usesRemaining;
    private int stackCount = 1;

    public Consumable(int uses) {
        if (uses <= 0) {
            throw new IllegalArgumentException("Consumables must have at least one use.");
        }
        this.maxUsesPerItem = uses;
        this.usesRemaining = uses;
        this.iconColor = new Color(220, 53, 69);
    }

    public Consumable() {
        this(1);
    }

    public void use(Player player, int currentTick) {
        if (usesRemaining > 0) {
            usesRemaining--;
            applyEffect(player, currentTick);
            if (usesRemaining == 0) {
                stackCount--;
                if (stackCount > 0) {
                    usesRemaining = maxUsesPerItem;
                }
            }
        }
    }

    public abstract void applyEffect(Player player, int currentTick);

    public boolean isUsed() {
        return stackCount <= 0;
    }

    public int getUsesRemaining() {
        return usesRemaining;
    }

    public int getStackCount() {
        return stackCount;
    }

    public int getMaxUsesPerItem() {
        return maxUsesPerItem;
    }

    public boolean canStackWith(Consumable other) {
        return other != null
                && other != this
                && stackCount > 0
                && other.stackCount > 0
                && getClass() == other.getClass()
                && maxUsesPerItem == other.maxUsesPerItem
                && usesRemaining == other.usesRemaining
                && Objects.equals(getStackingKey(), other.getStackingKey());
    }

    /**
     * Configurable consumables should include effect-defining values in this key.
     */
    protected Object getStackingKey() {
        return description;
    }

    /**
     * Transfers as many items as fit and returns the number transferred.
     */
    public int mergeFrom(Consumable other) {
        if (!canStackWith(other)) {
            return 0;
        }
        int transferred = Math.min(MAX_STACK_SIZE - stackCount, other.stackCount);
        stackCount += transferred;
        other.stackCount -= transferred;
        return transferred;
    }

    @Override
    public void tick(int currentTick, int clickXDown, int clickYDown, int clickXUp, int clickYUp,
                     ArrayList<VisualObject> targets, Player owner) {
        if (clickXDown > -1 && owner != null) {
            use(owner, currentTick);
        }
    }

    @Override
    public void leftClickAct(int clickXDown, int clickYDown, int clickXUp, int clickYUp, int currentTick, int processedX, int processedY) {
    }

    @Override
    public void draw(Graphics g) {
    }
}
