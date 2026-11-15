package Objects.PlayerClasses;

import GameRun.InputFrame;
import Objects.Entity;
import Objects.PlayerEquipment.Ability;
import Objects.PlayerEquipment.Armor;
import Objects.PlayerEquipment.ArmorSet;
import Objects.PlayerEquipment.Consumable;
import Objects.PlayerEquipment.Item;
import Objects.PlayerEquipment.MeleeWeapon;
import Objects.PlayerEquipment.Ultimate;
import Objects.PlayerEquipment.Weapons.Wand;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.util.Set;

public class Player extends Entity {

    // ── Gear slots ─────────────────────────────────────────────────────────
    public Armor headArmor = null;
    public Armor bodyArmor = null;
    protected double innateArmorReduction;
    private int armorSetSpeedBonus;
    private double armorSetSpellDamageBonus;

    // ── Ability slots (Q=0, E=1, F=2) ─────────────────────────────────────
    public Ability[] abilitySlots = new Ability[3];

    // ── Passive ability slots ──────────────────────────────────────────────
    public Ability[] passiveSlots = new Ability[2];

    private Ultimate ultimate;
    private double ultimateCharge;
    private double ultimateGuardReduction;
    private int ultimateGuardExpiresAt = -1;

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
        double total = innateArmorReduction;
        if (headArmor != null) total += headArmor.getArmorReduction();
        if (bodyArmor != null) total += bodyArmor.getArmorReduction();

        ArmorSet activeSet = getActiveArmorSet();
        int nextSpeedBonus = activeSet == null ? 0 : activeSet.getBonusMovementSpeed();
        total += activeSet == null ? 0.0 : activeSet.getBonusArmorReduction();
        total += ultimateGuardReduction;
        this.speed = (int) Math.round(this.speed - armorSetSpeedBonus + nextSpeedBonus);
        armorSetSpeedBonus = nextSpeedBonus;
        armorSetSpellDamageBonus = activeSet == null ? 0.0 : activeSet.getBonusSpellDamage();
        setTotalArmorReduction(total);
    }

    public void setInnateArmorReduction(double reduction) {
        innateArmorReduction = Math.max(0.0, reduction);
        recalcArmorReduction();
    }

    public int getArmorSetPieceCount(ArmorSet armorSet) {
        if (armorSet == null) return 0;
        int count = 0;
        if (headArmor != null && headArmor.getArmorSet() == armorSet) count++;
        if (bodyArmor != null && bodyArmor.getArmorSet() == armorSet) count++;
        return count;
    }

    public ArmorSet getActiveArmorSet() {
        ArmorSet headSet = headArmor == null ? null : headArmor.getArmorSet();
        if (headSet != null && getArmorSetPieceCount(headSet) >= ArmorSet.REQUIRED_PIECES) {
            return headSet;
        }
        ArmorSet bodySet = bodyArmor == null ? null : bodyArmor.getArmorSet();
        if (bodySet != null && getArmorSetPieceCount(bodySet) >= ArmorSet.REQUIRED_PIECES) {
            return bodySet;
        }
        return null;
    }

    public double getSpellDamageMultiplier() {
        return 1.0 + armorSetSpellDamageBonus;
    }

    public boolean addToHotbar(Item item) {
        if (item == null || item instanceof Ultimate) {
            return false;
        }
        if (item instanceof Consumable) {
            Consumable incoming = (Consumable) item;
            if (incoming.isUsed()) {
                return false;
            }
            mergeIntoHotbar(incoming);
            if (incoming.isUsed()) return true;
        }
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) {
                inventory[i] = item;
                return true;
            }
        }
        return false;
    }

    public void mergeIntoHotbar(Consumable incoming) {
        if (incoming == null || incoming.isUsed()) return;
        for (Item existing : inventory) {
            if (existing instanceof Consumable) {
                ((Consumable) existing).mergeFrom(incoming);
                if (incoming.isUsed()) return;
            }
        }
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

    public void update(Set<Integer> pressedKeys, int currentTick) {
        checkRolls(currentTick);
        checkOverHeal(currentTick);

        MeleeWeapon weapon = getEquippedMeleeWeapon();
        if (weapon != null) {
            weapon.setOwner(this);
            weapon.updateAttackAnimation();
        }

        ultimateCharge = Math.min(100.0, ultimateCharge + (100.0 / 6000.0));
        if (ultimateGuardExpiresAt != -1 && currentTick >= ultimateGuardExpiresAt) {
            ultimateGuardExpiresAt = -1;
            ultimateGuardReduction = 0.0;
            recalcArmorReduction();
        }

        this.hitbox.setRect(this.x, this.y, this.width, this.height);
        if (this.health <= 0) this.dead = true;
    }

    public void handleInput(InputFrame input) {
        if (input.wasPressed(73)) {
            inventoryOpen = !inventoryOpen;
        }
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
            Item item = inventory[MainHandIndex];
            item.tick(currentTick, clickXDown, clickYDown, -1, -1, null, this);
            if (item instanceof Consumable) {
                Consumable c = (Consumable) item;
                if (c.isUsed()) {
                    inventory[MainHandIndex] = null;
                }
            }
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

    public Ultimate getUltimate() { return ultimate; }

    public double getUltimateCharge() { return ultimateCharge; }

    public void setUltimate(Ultimate next) {
        if (next != null && !next.canBeUsedBy(this)) return;
        if (ultimate == next || (ultimate != null && next != null && ultimate.getId().equals(next.getId()))) return;
        ultimate = next;
        ultimateCharge = 0.0;
    }

    public void chargeUltimateForDamage(int damage) {
        if (damage > 0 && ultimate != null) ultimateCharge = Math.min(100.0, ultimateCharge + damage * 0.1);
    }

    public boolean tryActivateUltimate(Levels.BaseLevel level, int currentTick, int targetX, int targetY) {
        if (ultimate == null || ultimateCharge < 100.0) return false;
        ultimateCharge = 0.0;
        ultimate.activate(this, level, currentTick, targetX, targetY);
        return true;
    }

    public void grantUltimateGuard(int currentTick, int durationTicks, double reduction) {
        ultimateGuardReduction = Math.max(ultimateGuardReduction, reduction);
        ultimateGuardExpiresAt = Math.max(ultimateGuardExpiresAt, currentTick + durationTicks);
        recalcArmorReduction();
    }

    @Override
    public void hurt(int damage) {
        int before = health;
        super.hurt(damage);
        chargeUltimateForDamage(before - health);
    }

    // ── Visual ──────────────────────────────────────────────────────────────

    public void renderAttackEffects(Graphics g) {
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
    public void render(Graphics2D g, int tickCount) {
        renderAttackEffects(g);
        draw(g);
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
