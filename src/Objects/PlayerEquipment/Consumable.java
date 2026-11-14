package Objects.PlayerEquipment;

import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * Abstract base for consumable items (potions, etc.).
 * Single-use by default (usesRemaining = 1).
 */
public abstract class Consumable extends Item {

    protected int usesRemaining;

    public Consumable(int uses) {
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
        }
    }

    public abstract void applyEffect(Player player, int currentTick);

    public boolean isUsed() {
        return usesRemaining <= 0;
    }

    public int getUsesRemaining() {
        return usesRemaining;
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
