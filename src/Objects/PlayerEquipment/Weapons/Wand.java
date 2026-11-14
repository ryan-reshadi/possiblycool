package Objects.PlayerEquipment.Weapons;

import Levels.BaseLevel;
import Objects.EnemyClasses.Enemy;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Item;
import Objects.PlayerEquipment.Spells.*;
import Objects.Projectile;
import Objects.VisualObject;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Wand extends Item {

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
        this.name = name;
        this.firingCooldown = firingCooldown;
        this.attackPattern = pattern;
        this.iconColor = new Color(138, 43, 226);

        randomizeSpells();
        updateDescription();
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
            fire(clickXDown, clickYDown, currentTick, owner.getX() + owner.getWidth() / 2, owner.getY() + owner.getHeight() / 2);
        }
    }

    public void fire(int clickX, int clickY, int currentTick, int ownerCX, int ownerCY) {
        if (!isReady(currentTick) || level == null || activeSpell == null) return;
        lastFiredTick = currentTick;

        double baseAngle = Math.atan2(clickY - ownerCY, clickX - ownerCX);

        switch (attackPattern) {
            case SPREAD_3:
                spawnBolt(ownerCX, ownerCY, baseAngle - 0.2);
                spawnBolt(ownerCX, ownerCY, baseAngle);
                spawnBolt(ownerCX, ownerCY, baseAngle + 0.2);
                break;
            case RAPID_BURST:
                spawnBolt(ownerCX, ownerCY, baseAngle);
                spawnBolt(ownerCX + (int)(Math.cos(baseAngle) * 8), ownerCY + (int)(Math.sin(baseAngle) * 8), baseAngle);
                break;
            case SINGLE:
            default:
                spawnBolt(ownerCX, ownerCY, baseAngle);
                break;
        }
    }

    private void spawnBolt(int startX, int startY, double angle) {
        final Projectile[] projRef = new Projectile[1];
        projRef[0] = activeSpell.createProjectile(startX, startY, angle, () -> {
            if (level != null && projRef[0] != null) {
                level.queueRemoveVisualObject(projRef[0]);
            }
        });
        projRef[0].setEnemies(enemies);
        projRef[0].setPassives(passives);
        level.queueAddVisualObject(projRef[0]);
    }

    @Override
    public void leftClickAct(int clickXDown, int clickYDown, int clickXUp, int clickYUp, int currentTick, int processedX, int processedY) {
        fire(clickXDown, clickYDown, currentTick, processedX, processedY);
    }
}
