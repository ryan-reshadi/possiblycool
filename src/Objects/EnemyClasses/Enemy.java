package Objects.EnemyClasses;

import Objects.PlayerClasses.Player;
import Objects.Entity;
import Objects.PlayerEquipment.Item;
import Objects.Terrain.Border;
import Objects.Terrain.BossDoor;
import Objects.VisualObject;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import java.util.List;
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

    // Hit reaction. The force is applied over a short window so normal AI cannot
    // immediately cancel the displacement or start an attack while being struck.
    protected double knockbackX;
    protected double knockbackY;
    protected int knockbackTicksRemaining;
    protected int knockbackTicksTotal;
    protected int interruptedUntilTick = -1;
    protected int currentTick;
    protected List<? extends VisualObject> borders = java.util.Collections.emptyList();
    protected EnemyWeapon weapon;
    protected double dashAngle, dashSpeed;
    protected int dashTicks, dashTellTicks;
    protected double tellAngle;
    protected int tellLength;
    protected java.util.function.Consumer<VisualObject> projectileSink;

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

    public void setBorders(List<? extends VisualObject> borders) {
        this.borders = borders != null ? borders : java.util.Collections.emptyList();
    }
    public void setProjectileSink(java.util.function.Consumer<VisualObject> sink) { projectileSink = sink; }
    public void setWeapon(EnemyWeapon weapon) { this.weapon = weapon; }

    /** Move along each axis only when the enemy's hitbox remains clear of walls. */
    protected void moveWithWallCollision(int dx, int dy) {
        if (dx != 0 && !collidesWithWall(this.x + dx, this.y)) this.x += dx;
        if (dy != 0 && !collidesWithWall(this.x, this.y + dy)) this.y += dy;
        if (this.hitbox != null) this.hitbox.setRect(this.x, this.y, this.width, this.height);
    }

    private boolean collidesWithWall(int candidateX, int candidateY) {
        for (VisualObject wall : borders) {
            if (!(wall instanceof Border)) continue;
            Border border = (Border) wall;
            if (border instanceof BossDoor && !((BossDoor) border).isLocked()) continue;
            if (candidateX < border.getX() + border.getWidth()
                    && candidateX + this.width > border.getX()
                    && candidateY < border.getY() + border.getHeight()
                    && candidateY + this.height > border.getY()) {
                return true;
            }
        }
        return false;
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

    public void knockbackFrom(int sourceX, int sourceY, double force, int currentTick) {
        double dx = this.x + this.width / 2.0 - sourceX;
        double dy = this.y + this.height / 2.0 - sourceY;
        double distance = Math.hypot(dx, dy);
        if (distance == 0) {
            dx = 1;
            dy = 0;
            distance = 1;
        }
        this.knockbackX = dx / distance * force;
        this.knockbackY = dy / distance * force;
        this.knockbackTicksRemaining = Math.max(1, (int) Math.ceil(force / 2.0));
        this.knockbackTicksTotal = this.knockbackTicksRemaining;
        this.interruptedUntilTick = Math.max(this.interruptedUntilTick,
                currentTick + this.knockbackTicksRemaining + getAttackCooldownTicks());
        this.lastAttackInterrupted();
    }

    protected int getAttackCooldownTicks() {
        return 0;
    }

    /** Apply an incoming hit, interrupting the enemy's current attack pattern. */
    public void hit(int damage, int sourceX, int sourceY, double knockbackForce, int currentTick) {
        if (damage <= 0 || isDead()) return;
        hurt(damage);
        if (!isDead()) {
            knockbackFrom(sourceX, sourceY, knockbackForce, currentTick);
        }
    }

    /** Subclasses can clear any attack windup or queued attack when hit. */
    protected void lastAttackInterrupted() {
        // No pending attack state in the base enemy.
    }

    protected boolean isAttackInterrupted(int currentTick) {
        return currentTick < interruptedUntilTick;
    }

    protected boolean applyKnockback() {
        if (knockbackTicksRemaining <= 0) return false;
        double progress = (double) knockbackTicksRemaining / knockbackTicksTotal;
        moveWithWallCollision((int) Math.round(knockbackX * progress),
                (int) Math.round(knockbackY * progress));
        knockbackTicksRemaining--;
        return true;
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
        moveWithWallCollision((int) (wanderDx * spd), (int) (wanderDy * spd));
    }

    public void aggroLogic(int currentTick) {
        // Subclasses implement chase & attack
    }

    @Override
    public void tick(Graphics g, Set<Integer> pressedKeys,
                     int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount) {
        this.currentTick = tickCount;
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

        if (applyKnockback()) {
            // Movement is the hit reaction; skip AI for this tick.
        } else if (!isStunned(tickCount) && !isAttackInterrupted(tickCount)) {
            if (targetPlayer != null && !targetPlayer.isDead()) {
                aggroLogic(tickCount);
            } else {
                wander(tickCount);
            }
        }

        if (weapon instanceof BerserkerWeapon) drawDashTell(g);
        draw(g);
        if (weapon != null && targetPlayer != null) weapon.aim(this, targetPlayer, (java.awt.Graphics2D) g);
        drawHealthBar(g);
    }

    protected void drawDashTell(Graphics g) {
        if (dashTellTicks <= 0) return;
        g.setColor(dashTicks > 0 ? Color.RED : Color.WHITE);
        int cx = x + width / 2, cy = y + height / 2;
        g.drawLine(cx, cy, cx + (int)(Math.cos(tellAngle)*tellLength), cy + (int)(Math.sin(tellAngle)*tellLength));
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
