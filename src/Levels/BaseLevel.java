package Levels;

import Objects.*;
import Objects.EnemyClasses.*;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.*;
import Objects.PlayerEquipment.Armor.ArmorSlot;
import Objects.PlayerEquipment.Weapons.Dagger;
import Objects.PlayerEquipment.Weapons.Wand;
import Objects.Terrain.*;
import ShapeCore.AbilityBarUI;
import ShapeCore.HotbarUI;
import ShapeCore.InventoryPanel;
import ShapeCore.Buttons.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class BaseLevel {

    protected int width;
    protected int height;
    protected ArrayList<ArrayList<VisualObject>> levelVisualObjects = new ArrayList<>();
    protected Image backgroundImage;
    protected int bgX = 0;
    protected int bgY = 0;
    protected Player player;
    protected int buffer = 80;
    protected int offsetX = 0;
    protected int offsetY = 0;
    protected Color backgroundColor = null;

    protected List<VisualObject> addList = new LinkedList<>();
    protected List<VisualObject> removeList = new LinkedList<>();

    // UI Systems
    protected HotbarUI hotbarUI = new HotbarUI();
    protected AbilityBarUI abilityBarUI = new AbilityBarUI();
    protected InventoryPanel inventoryPanel = new InventoryPanel();

    protected int mouseX = 0;
    protected int mouseY = 0;
    protected boolean isRightClick = false;

    // Ability key debounces
    private boolean qWasDown = false;
    private boolean eWasDown = false;
    private boolean fWasDown = false;

    public BaseLevel(int bgX, int bgY, String path, ArrayList<VisualObject> players) {
        this.bgX = bgX;
        this.bgY = bgY;
        this.initArrays(players);
        try {
            File f = new File(path);
            if (f.exists()) {
                this.backgroundImage = ImageIO.read(f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.initLevel();
    }

    public BaseLevel(Color color, ArrayList<VisualObject> players) {
        this.initArrays(players);
        this.backgroundColor = color;
    }

    private void initArrays(ArrayList<VisualObject> players) {
        this.levelVisualObjects.add(players);               // 0: Player
        this.levelVisualObjects.add(new ArrayList<>());     // 1: Walls / Borders
        this.levelVisualObjects.add(new ArrayList<>());     // 2: Enemies
        this.levelVisualObjects.add(new ArrayList<>());     // 3: Buttons / UI
        this.levelVisualObjects.add(new ArrayList<>());     // 4: Projectiles / Effects / DroppedItems
        if (!players.isEmpty() && players.get(0) instanceof Player) {
            this.player = (Player) players.get(0);
        }
    }

    public void setMousePosition(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    public void setRightClick(boolean rightClick) {
        this.isRightClick = rightClick;
    }

    public List<Enemy> getEnemies() {
        List<Enemy> list = new ArrayList<>();
        if (this.levelVisualObjects.size() > 2) {
            for (VisualObject obj : this.levelVisualObjects.get(2)) {
                if (obj instanceof Enemy && !((Enemy) obj).isDead()) {
                    list.add((Enemy) obj);
                }
            }
        }
        return list;
    }

    public void queueAddVisualObject(VisualObject obj) {
        if (obj != null) {
            addList.add(obj);
        }
    }

    public void queueRemoveVisualObject(VisualObject obj) {
        if (obj != null) {
            removeList.add(obj);
        }
    }

    public void collectItem(Item item) {
        this.inventoryPanel.addItem(item);
    }

    public void processQueuedVisualObjectChanges() {
        for (VisualObject obj : removeList) {
            for (ArrayList<VisualObject> row : levelVisualObjects) {
                row.remove(obj);
            }
        }
        removeList.clear();

        for (VisualObject obj : addList) {
            addVisualObject(obj);
        }
        addList.clear();
    }

    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount) {
        this.tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, tickCount, 0);
    }

    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount, int scrollDelta) {
        this.drawBG(g);

        if (this.player != null && !this.player.isDead()) {
            // Feed current enemy list and level into equipped weapon
            List<Enemy> activeEnemies = getEnemies();
            Item equipped = this.player.getEquippedItem();
            if (equipped instanceof MeleeWeapon) {
                ((MeleeWeapon) equipped).setEnemies(activeEnemies);
            } else if (equipped instanceof Wand) {
                Wand wand = (Wand) equipped;
                wand.setEnemies(activeEnemies);
                wand.setLevel(this);
            }

            // If inventory is open, clicks interact with inventory panel instead of game world
            if (this.player.isInventoryOpen()) {
                this.inventoryPanel.handlePointer(clickXDown, clickYDown, clickXUp, clickYUp,
                        this.isRightClick, this.player, 1900, 1000, tickCount);
            } else {
                this.keyHandler(pressedKeys, clickXDown, clickYDown, tickCount);
                this.mouseHandler(clickXDown, clickYDown, tickCount);
                this.scrollHandler(scrollDelta);
                this.checkInteractions(pressedKeys, tickCount);
            }
        }

        // Tick all objects
        for (ArrayList<VisualObject> row : this.levelVisualObjects) {
            for (int i = row.size() - 1; i >= 0; i--) {
                if (i < row.size()) {
                    VisualObject obj = row.get(i);
                    if (obj != null) {
                        if (obj instanceof Player) {
                            ((Player) obj).tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, tickCount, this.levelVisualObjects.get(2));
                        } else if (obj instanceof Enemy) {
                            Enemy enemy = (Enemy) obj;
                            enemy.setBorders(this.levelVisualObjects.get(1));
                            enemy.setProjectileSink(this::queueAddVisualObject);
                            enemy.checkAggro(this.player);
                            obj.tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, tickCount);
                        } else {
                            obj.tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, tickCount);
                        }
                    }
                }
            }
        }

        // Follow after movement so the camera responds to the player's current
        // position instead of chasing the previous frame's position.
        if (this.player != null && !this.player.isDead()) {
            this.updateOffset();
        }

        // Draw HUD overlay
        this.displayUI(g, tickCount);

        // Draw Inventory Panel if open
        if (this.player != null && this.player.isInventoryOpen()) {
            this.inventoryPanel.draw(g, this.player, this.mouseX, this.mouseY, 1900, 1000);
        }

        this.processQueuedVisualObjectChanges();
    }

    private void checkInteractions(Set<Integer> pressedKeys, int currentTick) {
        if (this.player == null) return;

        // Checkpoints & Chests & Dropped items
        for (ArrayList<VisualObject> row : this.levelVisualObjects) {
            for (VisualObject obj : row) {
                if (obj instanceof Checkpoint) {
                    ((Checkpoint) obj).checkActivation(this.player);
                } else if (obj instanceof Chest) {
                    ((Chest) obj).tryOpen(this.player, pressedKeys);
                } else if (obj instanceof DroppedItem) {
                    ((DroppedItem) obj).checkPickup(this.player);
                }
            }
        }

        // Abilities: Q=81, E=69, F=70
        boolean qDown = pressedKeys.contains(81);
        if (qDown && !qWasDown) {
            this.player.tryActivateAbility(0, this, currentTick, mouseX, mouseY);
        }
        qWasDown = qDown;

        boolean eDown = pressedKeys.contains(69);
        if (eDown && !eWasDown) {
            // E triggers ability 1 if not opening a chest
            this.player.tryActivateAbility(1, this, currentTick, mouseX, mouseY);
        }
        eWasDown = eDown;

        boolean fDown = pressedKeys.contains(70);
        if (fDown && !fWasDown) {
            this.player.tryActivateAbility(2, this, currentTick, mouseX, mouseY);
        }
        fWasDown = fDown;
    }

    private void displayUI(Graphics g, int currentTick) {
        if (this.player == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Health Bar (Bottom Left)
        int hx = 30;
        int hy = 910;
        int barW = 200;
        int barH = 18;

        g2d.setColor(new Color(25, 25, 30, 200));
        g2d.fillRoundRect(hx - 8, hy - 26, barW + 16, barH + 46, 10, 10);
        g2d.setColor(new Color(70, 70, 85));
        g2d.drawRoundRect(hx - 8, hy - 26, barW + 16, barH + 46, 10, 10);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.setColor(Color.WHITE);
        g2d.drawString("HP: " + player.getHealth() + " / " + player.getMaxHealth(), hx, hy - 8);

        // Armor reduction display
        if (player.getTotalArmorReduction() > 0) {
            g2d.setColor(new Color(100, 180, 255));
            g2d.drawString(String.format("Armor: %.0f%%", player.getTotalArmorReduction() * 100), hx + 120, hy - 8);
        }

        g2d.setColor(new Color(50, 20, 20));
        g2d.fillRoundRect(hx, hy, barW, barH, 6, 6);

        int currentW = (int) Math.max(0, ((double) player.getHealth() / player.getMaxHealth()) * barW);
        g2d.setColor(new Color(220, 40, 40));
        g2d.fillRoundRect(hx, hy, currentW, barH, 6, 6);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(hx, hy, barW, barH, 6, 6);

        // Rolls indicator pips
        int rollX = hx;
        int rollY = hy + 24;
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2d.setColor(new Color(200, 200, 220));
        g2d.drawString("Rolls [R]:", rollX, rollY);
        for (int r = 0; r < player.maxRolls; r++) {
            int rx = rollX + 55 + r * 16;
            if (r < player.rolls) {
                g2d.setColor(new Color(255, 215, 0));
                g2d.fillOval(rx, rollY - 9, 10, 10);
            } else {
                g2d.setColor(new Color(80, 80, 90));
                g2d.drawOval(rx, rollY - 9, 10, 10);
            }
        }

        // 2. Ability Bar (Q, E, F)
        abilityBarUI.draw(g, player.getAbilitySlots(), currentTick, 1900, 1000);

        // 3. Hotbar (1-9)
        hotbarUI.draw(g, player.getInventory(), player.getMainHandIndex(), 1900, 1000);

        // Inventory toggle hint
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2d.setColor(new Color(160, 160, 180));
        g2d.drawString("[I] Inventory", 1780, 970);
    }

    public void drawBG(Graphics g) {
        if (this.backgroundImage != null) {
            g.drawImage(this.backgroundImage, this.bgX, this.bgY, null);
        } else {
            g.setColor(this.backgroundColor != null ? this.backgroundColor : new Color(30, 30, 35));
            g.fillRect(0, 0, 1900, 1000);
        }
    }

    public void addVisualObject(VisualObject obj) {
        if (obj != null) {
            if (obj instanceof ShapeCore.Buttons.Button) {
                this.levelVisualObjects.get(3).add(obj);
            } else if (obj instanceof Border) {
                this.levelVisualObjects.get(1).add(obj);
            } else if (obj instanceof Enemy) {
                this.levelVisualObjects.get(2).add(obj);
            } else if (obj instanceof Player) {
                this.levelVisualObjects.get(0).add(obj);
            } else {
                // Projectiles, DroppedItems, Effects, Checkpoints, Chests
                if (this.levelVisualObjects.size() > 4) {
                    this.levelVisualObjects.get(4).add(obj);
                } else {
                    this.levelVisualObjects.get(0).add(obj);
                }
            }
        }
    }

    public void keyHandler(Set<Integer> pressedKeys, int clickXDown, int clickYDown, int tickCount) {
        if (player == null) return;

        int speed = (int) player.getSpeed();
        int moveX = 0;
        int moveY = 0;

        // Roll mechanic: R (82)
        if (pressedKeys.contains(82) && player.rolls > 0) {
            int rollDist = 40;
            if (pressedKeys.contains(65)) moveX -= rollDist * speed;
            if (pressedKeys.contains(68)) moveX += rollDist * speed;
            if (pressedKeys.contains(87)) moveY -= rollDist * speed;
            if (pressedKeys.contains(83)) moveY += rollDist * speed;
            player.rollCooldown(tickCount, 180);
        }

        if (!player.isWeaponAnimating()) {
            if (pressedKeys.contains(65)) moveX -= speed;
            if (pressedKeys.contains(68)) moveX += speed;
            if (pressedKeys.contains(87)) moveY -= speed;
            if (pressedKeys.contains(83)) moveY += speed;

            // Number keys 1-9 (keycodes 49-57)
            for (int i = 1; i <= 9; i++) {
                if (pressedKeys.contains(48 + i)) {
                    player.setMainHandIndex(i - 1);
                    break;
                }
            }
        }

        int finalX = player.getX();
        int finalY = player.getY();

        if (moveX != 0) {
            int stepX = moveX > 0 ? 1 : -1;
            for (int i = 0; i < Math.abs(moveX); i++) {
                int testX = finalX + stepX;
                boolean collides = false;
                for (VisualObject obj : this.levelVisualObjects.get(1)) {
                    if (obj instanceof Border) {
                        Border b = (Border) obj;
                        if (b instanceof BossDoor && !((BossDoor) b).isLocked()) {
                            continue; // Can pass through unlocked boss door
                        }
                        if (testX < b.getX() + b.getWidth()
                                && testX + player.getWidth() > b.getX()
                                && finalY < b.getY() + b.getHeight()
                                && finalY + player.getHeight() > b.getY()) {
                            collides = true;
                            break;
                        }
                    }
                }
                if (collides) break;
                finalX = testX;
            }
        }

        if (moveY != 0) {
            int stepY = moveY > 0 ? 1 : -1;
            for (int i = 0; i < Math.abs(moveY); i++) {
                int testY = finalY + stepY;
                boolean collides = false;
                for (VisualObject obj : this.levelVisualObjects.get(1)) {
                    if (obj instanceof Border) {
                        Border b = (Border) obj;
                        if (b instanceof BossDoor && !((BossDoor) b).isLocked()) {
                            continue;
                        }
                        if (finalX < b.getX() + b.getWidth()
                                && finalX + player.getWidth() > b.getX()
                                && testY < b.getY() + b.getHeight()
                                && testY + player.getHeight() > b.getY()) {
                            collides = true;
                            break;
                        }
                    }
                }
                if (collides) break;
                finalY = testY;
            }
        }

        player.setX(finalX);
        player.setY(finalY);
    }

    public void mouseHandler(int clickXDown, int clickYDown, int currentTick) {
        if (clickXDown == -1 || clickYDown == -1 || player == null) return;
        this.player.attack(clickXDown, clickYDown, currentTick);
    }

    public void scrollHandler(int scrollDelta) {
        if (scrollDelta != 0 && this.player != null) {
            int currentIndex = this.player.getMainHandIndex();
            int newIndex = currentIndex + scrollDelta;
            if (newIndex < 0) newIndex = 8;
            else if (newIndex > 8) newIndex = 0;
            this.player.setMainHandIndex(newIndex);
        }
    }

    protected void updateOffset() {
        if (this.player == null) return;

        int dx = 0;
        int dy = 0;

        // Leave the camera still until the player approaches a screen edge. Then
        // ease the scene toward the player's screen center, with a capped step so
        // camera motion stays smooth and doesn't feel like a snap.
        int screenX = this.player.getX() + this.player.getWidth() / 2;
        int screenY = this.player.getY() + this.player.getHeight() / 2;
        int horizontalMargin = 100;
        int verticalMargin = 80;
        int centerX = 1900 / 2;
        int centerY = 1000 / 2;
        int left = horizontalMargin;
        int right = 1900 - horizontalMargin;
        int top = verticalMargin;
        int bottom = 1000 - verticalMargin;

        if (screenX < left || screenX > right) {
            dx = smoothCameraStep(centerX - screenX);
        }
        if (screenY < top || screenY > bottom) {
            dy = smoothCameraStep(centerY - screenY);
        }

        if (dx != 0 || dy != 0) {
            this.offsetX += dx;
            this.offsetY += dy;
            this.bgX += dx;
            this.bgY += dy;
            for (ArrayList<VisualObject> row : this.levelVisualObjects) {
                for (VisualObject obj : row) {
                    if (obj != null && obj.moveable) {
                        obj.move(dx, dy);
                    }
                }
            }
        }
    }

    private int smoothCameraStep(int distance) {
        // Move a substantial fraction of the remaining distance each frame,
        // while capping large jumps. This converges to center smoothly and
        // avoids the slow, repeated edge correction of the previous behavior.
        int step = (int) Math.round(distance * 0.22);
        if (step == 0 && distance != 0) step = Integer.signum(distance);
        return Math.max(-48, Math.min(48, step));
    }

    protected void initLevel() {
    }

    public void addBorder(Border b) {
        this.levelVisualObjects.get(1).add(b);
    }

    public void addEnemy(Enemy e) {
        this.levelVisualObjects.get(2).add(e);
    }

    public void addButton(ShapeCore.Buttons.Button b) {
        this.levelVisualObjects.get(3).add(b);
    }

    public InventoryPanel getInventoryPanel() {
        return inventoryPanel;
    }
}
