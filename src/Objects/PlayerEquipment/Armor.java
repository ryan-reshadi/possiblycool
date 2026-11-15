package Objects.PlayerEquipment;

import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * Armor item. Reduces incoming damage by a percentage.
 * HEAD slot: max 20% reduction. BODY slot: max 40% reduction.
 */
public class Armor extends Item {

    public enum ArmorSlot {
        HEAD, BODY
    }

    private static final double HEAD_MAX_REDUCTION = 0.20;
    private static final double BODY_MAX_REDUCTION = 0.40;

    private final double armorReduction; // 0.0 – 1.0, capped by slot
    private final ArmorSlot slot;
    private final ArmorSet armorSet;
    private int level = 1;

    public Armor(String name, String description, double armorReduction, ArmorSlot slot) {
        this(name, description, armorReduction, slot, null);
    }

    public Armor(String name, String description, double armorReduction, ArmorSlot slot, ArmorSet armorSet) {
        this.name = name;
        this.description = description;
        this.slot = slot;
        this.armorSet = armorSet;
        this.iconColor = (slot == ArmorSlot.HEAD) ? new Color(100, 149, 237) : new Color(65, 105, 225);
        this.spritePath = (slot == ArmorSlot.HEAD) ? "images/drops/helmet.png" : "images/drops/chestplate.png";

        double cap = (slot == ArmorSlot.HEAD) ? HEAD_MAX_REDUCTION : BODY_MAX_REDUCTION;
        this.armorReduction = Math.max(0.0, Math.min(cap, armorReduction));
    }

    public double getArmorReduction() {
        double scaledReduction = armorReduction * (1.0 + 0.05 * (level - 1));
        double cap = slot == ArmorSlot.HEAD ? HEAD_MAX_REDUCTION : BODY_MAX_REDUCTION;
        return Math.min(cap, scaledReduction);
    }

    public int getLevel() { return level; }

    public void setLevel(int level) { this.level = Math.max(1, level); }

    public void restoreProgression(int level) { setLevel(level); }

    public static int rollLevelNear(int referenceLevel, java.util.Random random) {
        if (random == null) throw new IllegalArgumentException("Random source cannot be null.");
        int roll = random.nextInt(100);
        int offset = roll < 15 ? -1 : roll < 50 ? 0 : 1;
        return Math.max(1, referenceLevel + offset);
    }

    public ArmorSlot getSlot() {
        return slot;
    }

    public ArmorSet getArmorSet() {
        return armorSet;
    }

    @Override
    public void tick(int currentTick, int clickXDown, int clickYDown, int clickXUp, int clickYUp,
                     ArrayList<VisualObject> targets, Player owner) {
        // Passive — no tick logic needed
    }

    @Override
    public void leftClickAct(int clickXDown, int clickYDown, int clickXUp, int clickYUp, int currentTick, int processedX, int processedY) {
        // Armor has no active left-click action
    }

    @Override
    public void draw(Graphics g) {
        // Armor is drawn in inventory/HUD
    }
}
