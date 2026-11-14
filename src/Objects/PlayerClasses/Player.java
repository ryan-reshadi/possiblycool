package Objects.PlayerClasses;

import Objects.Entity;
import Objects.PlayerEquipment.Ability;
import Objects.PlayerEquipment.Armor;
import Objects.PlayerEquipment.Item;
import Objects.PlayerEquipment.MeleeWeapon;
import Objects.PlayerEquipment.Weapons.Wand;
import Objects.VisualObject;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Set;

public class Player extends Entity {

    // ── Gear slots ─────────────────────────────────────────────────────────
    public Armor headArmor = null;
    public Armor bodyArmor = null;

    // ── Ability slots (Q=0, E=1, F=2) ─────────────────────────────────────
    public Ability[] abilitySlots = new Ability[3];

    // ── Passive ability slots ──────────────────────────────────────────────
    public Ability[] passiveSlots = new Ability[2];

    // ── Roll system ────────────────────────────────────────────────────────
    protected double rollSpeed = 1.0;
    public int maxRolls = 1;
    public int rolls = maxRolls;
    public int rollRechargeTick = -1;

    // ── Overheal ───────────────────────────────────────────────────────────
    protected int overHealAmount = 0;
    protected int overHealExpireTime = -1;

    // ── Death flag (checked by BaseLevel / Game) ───────────────────────────
    public boolean dead = false;

    // ── Checkpoint position (saved on activation) ─────────────────────────
    public int checkpointX = -1;
    public int checkpointY = -1;

    // ── Inventory panel toggle key debounce ────────────────────────────────
    private boolean inventoryKeyWasDown = false;
    public boolean inventoryOpen = false;

    public Player(int x, int y, int maxHealth) {
        super(x, y, "images/rus.jpg");
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.width = 50;
        this.height = 50;
        this.hitbox = new Rectangle2D.Double(x, y, width, height);
    }

    // ── Equip helpers ───────────────────────────────────────────────────────

    public void equipHead(Armor a) {
        headArmor = a;
        recalcArmorReduction();
    }

    public void equipBody(Armor a) {
        bodyArmor = a;
        recalcArmorReduction();
    }

    public void recalcArmorReduction() {
        double total = 0.0;
        if (headArmor != null) total += headArmor.getArmorReduction();
        if (bodyArmor != null) total += bodyArmor.getArmorReduction();
        setTotalArmorReduction(total);
    }

    public boolean addToHotbar(Item item) {
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) {
                inventory[i] = item;
                return true;
            }
        }
        return false;
    }

    public boolean addAbility(Ability a) {
        for (int i = 0; i < abilitySlots.length; i++) {
            if (abilitySlots[i] == null) {
                abilitySlots[i] = a;
                return true;
            }
        }
        return false;
    }

    // ── Weapon helpers ──────────────────────────────────────────────────────

    public Item getEquippedItem() {
        if (MainHandIndex >= 0 && MainHandIndex < inventory.length) {
            return inventory[MainHandIndex];
        }
        return null;
    }

    public MeleeWeapon getEquippedMeleeWeapon() {
        Item item = getEquippedItem();
        if (item instanceof MeleeWeapon) {
            return (MeleeWeapon) item;
        }
        return null;
    }

    public boolean isWeaponAnimating() {
        MeleeWeapon w = getEquippedMeleeWeapon();
        return w != null && w.isInAnimation();
    }

    // ── Per-tick logic ──────────────────────────────────────────────────────

    public void checkAllTick(int currentTick, Set<Integer> pressedKeys,
                             int clickXDown, int clickYDown,
                             int clickXUp, int clickYUp,
                             ArrayList<VisualObject> others) {
        checkRolls(currentTick);
        checkOverHeal(currentTick);

        MeleeWeapon weapon = getEquippedMeleeWeapon();
        if (weapon != null) {
            weapon.updateAttackAnimation();
        }

        // Inventory toggle (I key = 73)
        boolean iDown = pressedKeys.contains(73);
        if (iDown && !inventoryKeyWasDown) {
            inventoryOpen = !inventoryOpen;
        }
        inventoryKeyWasDown = iDown;
    }

    // ── Overheal ────────────────────────────────────────────────────────────

    public void overHeal(int amount, int currentTick, int duration) {
        this.overHealAmount += amount;
        this.overHealExpireTime = currentTick + duration;
    }

    public void checkOverHeal(int currentTick) {
        if (this.overHealAmount > 0 && currentTick >= this.overHealExpireTime) {
            this.overHealAmount = 0;
            this.overHealExpireTime = -1;
        }
    }

    // ── Rolls ───────────────────────────────────────────────────────────────

    public void checkRolls(int currentTick) {
        if (rollRechargeTick != -1 && currentTick >= rollRechargeTick) {
            rolls = Math.min(rolls + 1, maxRolls);
            rollRechargeTick = -1;
            if (rolls < maxRolls) {
                rollRechargeTick = currentTick + (int) (180 / rollSpeed);
            }
        }
    }

    public void rollCooldown(int currentTick, int cooldownTime) {
        if (rollRechargeTick == -1) {
            rollRechargeTick = currentTick + cooldownTime;
        }
        rolls = Math.max(0, rolls - 1);
    }

    // ── Attack ──────────────────────────────────────────────────────────────

    public void attack(int clickXDown, int clickYDown, int currentTick) {
        if (MainHandIndex >= 0 && MainHandIndex < inventory.length && inventory[MainHandIndex] != null) {
            inventory[MainHandIndex].tick(currentTick, clickXDown, clickYDown, -1, -1, null, this);
        }
    }

    // ── Ability activation ──────────────────────────────────────────────────

    public void tryActivateAbility(int slot, Levels.BaseLevel level, int currentTick,
                                   int targetX, int targetY) {
        if (slot < 0 || slot >= abilitySlots.length) return;
        Ability a = abilitySlots[slot];
        if (a != null && a.isReady(currentTick)) {
            a.setTarget(targetX, targetY);
            a.activate(this, level, currentTick);
        }
    }

    // ── Visual ──────────────────────────────────────────────────────────────

    public void checkAttackEffects(Graphics g, int currentTick) {
        Graphics2D g2d = (Graphics2D) g;
        MeleeWeapon weapon = getEquippedMeleeWeapon();
        if (weapon != null && weapon.getAttackBox() != null && weapon.isInAnimation()) {
            if (weapon.isAttackDamageApplied()) {
                g2d.setColor(new Color(255, 60, 60, 160));
            } else {
                g2d.setColor(new Color(255, 255, 255, 120));
            }
            g2d.fill(weapon.getAttackBox());
        }
    }

    // ── Main tick ───────────────────────────────────────────────────────────

    @Override
    public void tick(Graphics g, Set<Integer> pressedKeys,
                     int clickXDown, int clickYDown,
                     int clickXUp, int clickYUp, int tickCount) {
        this.draw(g);
    }

    public void tick(Graphics g, Set<Integer> pressedKeys,
                     int clickXDown, int clickYDown,
                     int clickXUp, int clickYUp, int tickCount,
                     ArrayList<VisualObject> others) {
        this.width = 50;
        this.height = 50;
        this.hitbox.setRect(this.x, this.y, this.width, this.height);

        checkAllTick(tickCount, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, others);
        checkAttackEffects(g, tickCount);
        this.draw(g);

        if (this.health <= 0 && !this.dead) {
            this.dead = true;
        }
    }

    // ── Checkpoint ──────────────────────────────────────────────────────────

    public void saveCheckpoint() {
        checkpointX = this.x;
        checkpointY = this.y;
    }

    public void respawnAtCheckpoint(int defaultStartX, int defaultStartY) {
        if (checkpointX >= 0) {
            this.x = checkpointX;
            this.y = checkpointY;
        } else {
            this.x = defaultStartX;
            this.y = defaultStartY;
        }
        this.health = this.maxHealth;
        this.dead = false;
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    @Override public int getHealth() { return this.health; }
    @Override public int getMaxHealth() { return this.maxHealth; }
    public Ability[] getAbilitySlots() { return abilitySlots; }
    public Ability[] getPassiveSlots() { return passiveSlots; }
    public Armor getHeadArmor() { return headArmor; }
    public Armor getBodyArmor() { return bodyArmor; }
    public boolean isInventoryOpen() { return inventoryOpen; }
    public void setInventoryOpen(boolean open) { this.inventoryOpen = open; }
}
