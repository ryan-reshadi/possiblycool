package Objects.EnemyClasses;

import Objects.PlayerClasses.Player;
import Objects.Entity;
import Objects.PlayerEquipment.Item;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import java.util.Set;

public class Enemy extends Entity {

    protected int damage;
    protected int aggroRange;
    protected Player targetPlayer = null;

    // Wandering AI
    protected int wanderTimer = 0;
    protected int wanderDx = 0;
    protected int wanderDy = 0;
    protected static final Random random = new Random();

    // Status effects
    protected int burnEndTick = -1;
    protected int poisonEndTick = -1;
    protected int slowEndTick = -1;
    protected int stunnedUntilTick = -1;

    // Drop table callback
    protected Runnable onDeath = null;
    protected boolean deathHandled = false;

    public Enemy(int x, int y, int health, int damage, int aggroRange, String imgPath) {
        super(x, y, imgPath);
        this.maxHealth = health;
        this.health = health;
        this.damage = damage;
        this.aggroRange = aggroRange;
        this.speed = 2;
        this.hitbox = new Rectangle2D.Double(x, y, 40, 40);
        this.width = 40;
        this.height = 40;
    }

    public Enemy(int x, int y, int width, int height, int health, int damage, int aggroRange, Color color) {
        super(x, y, width, height, color);
        this.maxHealth = health;
        this.health = health;
        this.damage = damage;
        this.aggroRange = aggroRange;
        this.speed = 2;
        this.hitbox = new Rectangle2D.Double(x, y, width, height);
    }

    public void setOnDeath(Runnable onDeath) {
        this.onDeath = onDeath;
    }

    public void checkAggro(Player target) {
        if (target != null && !target.isDead()) {
            double distance = Math.hypot(target.getX() - this.x, target.getY() - this.y);
            if (distance <= this.aggroRange) {
                this.targetPlayer = target;
            } else {
                this.targetPlayer = null;
            }
        } else {
            this.targetPlayer = null;
        }
    }

    public double getDistanceToPlayer() {
        if (this.targetPlayer != null) {
            return Math.hypot(this.targetPlayer.getX() - this.x, this.targetPlayer.getY() - this.y);
        }
        return Double.MAX_VALUE;
    }

    public void applyBurn(int currentTick) {
        this.burnEndTick = currentTick + 180; // 3 seconds
    }

    public void applyPoison(int currentTick) {
        this.poisonEndTick = currentTick + 300; // 5 seconds
    }

    public void applySlow(int currentTick) {
        this.slowEndTick = currentTick + 120; // 2 seconds
    }

    public void stun(int ticks, int currentTick) {
        this.stunnedUntilTick = currentTick + ticks;
    }

    public boolean isStunned(int currentTick) {
        return currentTick < stunnedUntilTick;
    }

    protected void processStatusEffects(int currentTick) {
        if (currentTick < burnEndTick && currentTick % 30 == 0) {
            hurtTrue(3); // 6 damage per second
        }
        if (currentTick < poisonEndTick && currentTick % 40 == 0) {
            hurtTrue(2);
        }
    }

    public double getCurrentSpeed(int currentTick) {
        double spd = this.speed;
        if (currentTick < slowEndTick) {
            spd *= 0.5;
        }
        return spd;
    }

    public void wander(int currentTick) {
        wanderTimer--;
        if (wanderTimer <= 0) {
            wanderTimer = 60 + random.nextInt(90);
            int dir = random.nextInt(5); // 0=stay, 1=up, 2=down, 3=left, 4=right
            wanderDx = 0;
            wanderDy = 0;
            if (dir == 1) wanderDy = -1;
            else if (dir == 2) wanderDy = 1;
            else if (dir == 3) wanderDx = -1;
            else if (dir == 4) wanderDx = 1;
        }
        double spd = getCurrentSpeed(currentTick);
        this.x += (int) (wanderDx * spd);
        this.y += (int) (wanderDy * spd);
    }

    public void aggroLogic(int currentTick) {
        // Subclasses implement chase & attack
    }

    @Override
    public void tick(Graphics g, Set<Integer> pressedKeys,
                     int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount) {
        if (isDead()) {
            if (!deathHandled) {
                deathHandled = true;
                this.visible = false;
                if (onDeath != null) {
                    onDeath.run();
                }
            }
            return;
        }

        if (this.hitbox != null) {
            this.hitbox.setRect(this.x, this.y, this.width, this.height);
        }

        processStatusEffects(tickCount);

        if (!isStunned(tickCount)) {
            if (targetPlayer != null && !targetPlayer.isDead()) {
                aggroLogic(tickCount);
            } else {
                wander(tickCount);
            }
        }

        draw(g);
        drawHealthBar(g);
    }

    protected void drawHealthBar(Graphics g) {
        if (this.health < this.maxHealth && this.health > 0) {
            int barWidth = this.width;
            int barHeight = 4;
            int barX = this.x;
            int barY = this.y - 7;
            g.setColor(Color.DARK_GRAY);
            g.fillRect(barX, barY, barWidth, barHeight);
            int currentWidth = (int) ((double) this.health / this.maxHealth * barWidth);
            g.setColor(Color.RED);
            g.fillRect(barX, barY, Math.max(1, currentWidth), barHeight);
        }
    }
}
