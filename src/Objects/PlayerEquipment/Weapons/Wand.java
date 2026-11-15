package Objects.PlayerEquipment.Weapons;

import Levels.BaseLevel;
import Objects.EnemyClasses.Enemy;
import Objects.PlayerClasses.Player;
import Objects.PlayerClasses.WizardPlayer;
import Objects.PlayerEquipment.PlayerWeapon;
import Objects.PlayerEquipment.Spells.*;
import Objects.Projectile;
import Objects.VisualObject;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Wand extends PlayerWeapon {

    public enum AttackPattern {
        SINGLE, SPREAD_3, RAPID_BURST
    }

    protected AttackPattern attackPattern = AttackPattern.SINGLE;
    protected int firingCooldown = 25; // ~0.4s
    protected int lastFiredTick = -1;

    protected ActiveSpell activeSpell;
    protected PassiveSpell[] passives = new PassiveSpell[2];

    protected BaseLevel level;
    protected List<Enemy> enemies;

    private static final Random random = new Random();

    public Wand(String name, int firingCooldown, AttackPattern pattern) {
        setAllowedClasses(WizardPlayer.class);
        this.name = name;
        this.firingCooldown = firingCooldown;
        this.attackPattern = pattern;
        this.iconColor = new Color(138, 43, 226);
        this.spritePath = "images/drops/wand.png";

        randomizeSpells();
        updateDescription();
    }

    public Wand(String name, int firingCooldown, AttackPattern pattern, double damageRoll, int weaponLevel) {
        this(name, firingCooldown, pattern);
        restoreProgression(damageRoll, weaponLevel);
    }

    public Wand(String name, int firingCooldown) {
        this(name, firingCooldown, AttackPattern.SINGLE);
    }

    public Wand() {
        this("Enchanted Wand", 25, AttackPattern.SINGLE);
    }

    public void setLevel(BaseLevel level) {
        this.level = level;
    }

    public void setEnemies(List<Enemy> enemies) {
        this.enemies = enemies;
    }

    public void setAttackPattern(AttackPattern pattern) {
        this.attackPattern = pattern;
        updateDescription();
    }

    public AttackPattern getAttackPattern() {
        return attackPattern;
    }

    public void setFiringCooldown(int ticks) {
        this.firingCooldown = Math.max(5, ticks);
        updateDescription();
    }

    public int getFiringCooldown() {
        return firingCooldown;
    }

    public int getDamage() {
        return activeSpell == null ? 0 : getScaledDamage(activeSpell.getDamage());
    }

    public int getDamage(Player owner) {
        return (int) Math.round(getDamage() * (owner == null ? 1.0 : owner.getSpellDamageMultiplier()));
    }

    public double getAttacksPerSecond() {
        return 60.0 / Math.max(1, firingCooldown);
    }

    public int getEffectiveRange() {
        return activeSpell == null ? 0
                : (int) Math.round(activeSpell.getProjectileSpeed() * Projectile.MAX_LIFETIME_TICKS);
    }

    public void setActiveSpell(ActiveSpell spell) {
        this.activeSpell = spell;
        updateDescription();
    }

    public ActiveSpell getActiveSpell() {
        return activeSpell;
    }

    public void setPassives(PassiveSpell p1, PassiveSpell p2) {
        this.passives[0] = p1;
        this.passives[1] = p2;
        updateDescription();
    }

    public PassiveSpell[] getPassives() {
        return passives;
    }

    public String getSavedActiveSpellType() {
        return activeSpell == null ? null : activeSpell.getClass().getSimpleName();
    }

    public List<String> getSavedPassiveSpellTypes() {
        return List.of(
                passives[0] == null ? "" : passives[0].getClass().getSimpleName(),
                passives[1] == null ? "" : passives[1].getClass().getSimpleName());
    }

    public void restoreSavedSpells(String activeType, String firstPassive, String secondPassive) {
        ActiveSpell restored = activeSpellFromType(activeType);
        if (restored != null) setActiveSpell(restored);
        if (firstPassive != null || secondPassive != null) {
            setPassives(passiveSpellFromType(firstPassive), passiveSpellFromType(secondPassive));
        }
    }

    private static ActiveSpell activeSpellFromType(String type) {
        if ("FireBoltSpell".equals(type)) return new FireBoltSpell();
        if ("IceBoltSpell".equals(type)) return new IceBoltSpell();
        if ("ArcaneSpell".equals(type)) return new ArcaneSpell();
        return null;
    }

    private static PassiveSpell passiveSpellFromType(String type) {
        if ("BurnPassive".equals(type)) return new BurnPassive();
        if ("SlowPassive".equals(type)) return new SlowPassive();
        if ("PoisonPassive".equals(type)) return new PoisonPassive();
        return null;
    }

    public void randomizeSpells() {
        ActiveSpell[] activePool = {
            new FireBoltSpell(),
            new IceBoltSpell(),
            new ArcaneSpell()
        };
        this.activeSpell = activePool[random.nextInt(activePool.length)];

        List<PassiveSpell> passivePool = new ArrayList<>();
        passivePool.add(new BurnPassive());
        passivePool.add(new SlowPassive());
        passivePool.add(new PoisonPassive());
        Collections.shuffle(passivePool, random);

        this.passives[0] = passivePool.get(0);
        this.passives[1] = passivePool.get(1);
    }

    private void updateDescription() {
        String p1Name = passives[0] != null ? passives[0].getName() : "None";
        String p2Name = passives[1] != null ? passives[1].getName() : "None";
        String spellName = activeSpell != null ? activeSpell.getName() : "None";
        this.description = spellName + " (" + attackPattern + ") | Passives: " + p1Name + ", " + p2Name;
    }

    public boolean isReady(int currentTick) {
        return lastFiredTick == -1 || currentTick >= lastFiredTick + firingCooldown;
    }

    @Override
    public void tick(int currentTick, int clickXDown, int clickYDown, int clickXUp, int clickYUp,
                     ArrayList<VisualObject> targets, Player owner) {
        if ((clickXDown > -1 || clickYDown > -1) && isReady(currentTick) && owner != null && level != null) {
            fire(clickXDown, clickYDown, currentTick, owner.getX() + owner.getWidth() / 2,
                    owner.getY() + owner.getHeight() / 2, owner);
        }
    }

    public void fire(int clickX, int clickY, int currentTick, int ownerCX, int ownerCY) {
        fire(clickX, clickY, currentTick, ownerCX, ownerCY, null);
    }

    private void fire(int clickX, int clickY, int currentTick, int ownerCX, int ownerCY, Player owner) {
        if (!isReady(currentTick) || level == null || activeSpell == null) return;
        lastFiredTick = currentTick;

        double baseAngle = Math.atan2(clickY - ownerCY, clickX - ownerCX);

        switch (attackPattern) {
            case SPREAD_3:
                spawnBolt(ownerCX, ownerCY, baseAngle - 0.2, owner);
                spawnBolt(ownerCX, ownerCY, baseAngle, owner);
                spawnBolt(ownerCX, ownerCY, baseAngle + 0.2, owner);
                break;
            case RAPID_BURST:
                spawnBolt(ownerCX, ownerCY, baseAngle, owner);
                spawnBolt(ownerCX + (int)(Math.cos(baseAngle) * 8),
                        ownerCY + (int)(Math.sin(baseAngle) * 8), baseAngle, owner);
                break;
            case SINGLE:
            default:
                spawnBolt(ownerCX, ownerCY, baseAngle, owner);
                break;
        }
    }

    private void spawnBolt(int startX, int startY, double angle, Player owner) {
        final Projectile[] projRef = new Projectile[1];
        projRef[0] = activeSpell.createProjectile(startX, startY, angle, getDamage(owner), () -> {
            if (level != null && projRef[0] != null) {
                level.queueRemoveVisualObject(projRef[0]);
            }
        });
        projRef[0].setEnemies(enemies);
        projRef[0].setOwner(owner);
        projRef[0].setPassives(passives);
        level.queueAddVisualObject(projRef[0]);
    }

    @Override
    public void leftClickAct(int clickXDown, int clickYDown, int clickXUp, int clickYUp, int currentTick, int processedX, int processedY) {
        fire(clickXDown, clickYDown, currentTick, processedX, processedY);
    }
}
