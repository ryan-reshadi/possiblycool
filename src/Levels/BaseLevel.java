package Levels;

import Objects.*;
import Objects.Projectile;
import Objects.EnemyClasses.*;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Consumable;
import Objects.PlayerEquipment.Item;
import Objects.PlayerEquipment.MeleeWeapon;
import Objects.PlayerEquipment.Armor.ArmorSlot;
import Objects.PlayerEquipment.Weapons.Dagger;
import Objects.PlayerEquipment.Weapons.Wand;
import Objects.Terrain.*;
import Objects.Effects.RallyBarricade;
import Levels.Generation.LevelBlueprint;
import Levels.Generation.RoomRestriction;
import ShapeCore.AbilityBarUI;
import ShapeCore.UltimateBarUI;
import ShapeCore.HotbarUI;
import ShapeCore.InventoryPanel;
import ShapeCore.Buttons.*;
import GameRun.InputFrame;
import GameRun.AssetLoader;
import GameRun.Viewport;
import GameRun.Game;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BaseLevel {

    protected int width;
    protected int height;
    protected ArrayList<ArrayList<VisualObject>> levelVisualObjects = new ArrayList<>();
    protected Image backgroundImage;
    protected int bgX = 0;
    protected int bgY = 0;
    protected Player player;
    protected int cameraX;
    protected int cameraY;
    protected Color backgroundColor = null;
    protected LevelObjective objective;

    protected List<VisualObject> addList = new LinkedList<>();
    protected List<VisualObject> removeList = new LinkedList<>();

    // UI Systems
    protected HotbarUI hotbarUI = new HotbarUI();
    protected AbilityBarUI abilityBarUI = new AbilityBarUI();
    protected UltimateBarUI ultimateBarUI = new UltimateBarUI();
    protected InventoryPanel inventoryPanel = new InventoryPanel();
    protected Runnable progressSaveListener = () -> { };

    protected final List<LevelBlueprint.RoomRegionSpec> roomRegions = new ArrayList<>();
    protected final List<LevelBlueprint.HazardZoneSpec> hazardZones = new ArrayList<>();
    protected final java.util.Map<String, Boolean> puzzleSolved = new java.util.HashMap<>();
    protected final List<java.util.AbstractMap.SimpleEntry<BossDoor, String>> puzzleLocks = new ArrayList<>();
    protected final List<java.util.AbstractMap.SimpleEntry<BossDoor, String>> itemKeyLocks = new ArrayList<>();

    protected int mouseX = 0;
    protected int mouseY = 0;
    private int hazardTickCounter = 0;

    public BaseLevel(int bgX, int bgY, String path, ArrayList<VisualObject> players) {
        this.bgX = bgX;
        this.bgY = bgY;
        this.initArrays(players);
        this.backgroundImage = path == null ? null : AssetLoader.loadImage(path);
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
        this.inventoryPanel.setChangeListener(() -> {
            if (this.player != null) this.progressSaveListener.run();
        });
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

    public boolean isComplete() {
        return objective != null && objective.isComplete(this);
    }

    public void queueAddVisualObject(VisualObject obj) {
        if (obj != null) {
            if (obj instanceof Projectile) {
                ((Projectile) obj).setWalls(this.levelVisualObjects.get(1));
            }
            addList.add(obj);
        }
    }

    public void queueRemoveVisualObject(VisualObject obj) {
        if (obj != null) {
            removeList.add(obj);
        }
    }

    public void collectItem(Item item) {
        if (player != null && item instanceof Consumable) {
            player.mergeIntoHotbar((Consumable) item);
            if (((Consumable) item).isUsed()) return;
        }
        this.inventoryPanel.addItem(item);
        progressSaveListener.run();
    }

    public void setProgressSaveListener(Runnable listener) {
        progressSaveListener = listener == null ? () -> { } : listener;
        inventoryPanel.setChangeListener(() -> {
            if (player != null) progressSaveListener.run();
        });
    }

    public void setCoinWallet(java.util.function.IntSupplier balance, java.util.function.IntConsumer reward) {
        inventoryPanel.setCoinWallet(balance, reward);
    }

    public List<Item> getStoredItems() {
        return inventoryPanel.getStorage();
    }

    public void setStoredItems(List<Item> items) {
        inventoryPanel.setStoredItems(items);
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

    public void update(InputFrame input, int tickCount) {
        this.mouseX = input.getMouseX();
        this.mouseY = input.getMouseY();

        if (this.player != null && !this.player.isDead()) {
            this.player.handleInput(input);
            List<Enemy> activeEnemies = getEnemies();
            Item equipped = this.player.getEquippedItem();
            if (equipped instanceof MeleeWeapon) {
                ((MeleeWeapon) equipped).setEnemies(activeEnemies);
            } else if (equipped instanceof Wand) {
                Wand wand = (Wand) equipped;
                wand.setEnemies(activeEnemies);
                wand.setLevel(this);
            }

            if (this.player.isInventoryOpen()) {
                this.inventoryPanel.handlePointer(input.getClickXDown(), input.getClickYDown(),
                        input.getClickXUp(), input.getClickYUp(), input.isRightClick(),
                        this.player, Viewport.WIDTH, Viewport.HEIGHT, tickCount);
                this.progressSaveListener.run();
            } else {
                this.keyHandler(input, tickCount);
                this.scrollHandler(input.getScrollDelta());
                if (input.getScrollDelta() != 0) this.progressSaveListener.run();
                boolean openedChest = this.checkInteractions(input, tickCount);
                boolean ultimateOverridesAttack = this.player.isUltimateWeaponOverrideActive() && !(equipped instanceof Consumable);
                if (!openedChest && input.hasMousePress() && !ultimateOverridesAttack) {
                    this.mouseHandler(input.getClickXDown() + cameraX,
                            input.getClickYDown() + cameraY, tickCount);
                }
            }
        }

        for (ArrayList<VisualObject> row : this.levelVisualObjects) {
            for (int i = row.size() - 1; i >= 0; i--) {
                if (i >= row.size()) continue;
                VisualObject obj = row.get(i);
                if (obj == null) continue;

                if (obj instanceof Player) {
                    ((Player) obj).update(input.getHeldKeys(), tickCount);
                } else if (obj instanceof Enemy) {
                    Enemy enemy = (Enemy) obj;
                    enemy.setBorders(this.levelVisualObjects.get(1));
                    enemy.setProjectileSink(this::queueAddVisualObject);
                    enemy.checkAggro(this.player);
                    enemy.update(tickCount);
                } else if (obj instanceof ShapeCore.Buttons.Button) {
                    ((ShapeCore.Buttons.Button) obj).update(input, tickCount);
                } else {
                    obj.update(tickCount);
                }
                if (!obj.isVisible()) queueRemoveVisualObject(obj);
            }
        }

        if (this.player != null && !this.player.isDead()) {
            this.player.tickUltimate(this, tickCount, input);
            // hazard effects
            hazardTickCounter++;
            if (hazardTickCounter >= 30) {
                hazardTickCounter = 0;
                int cx = player.getX() + player.getWidth()/2;
                int cy = player.getY() + player.getHeight()/2;
                for (LevelBlueprint.HazardZoneSpec hz : hazardZones) {
                    if (hz == null || hz.rect == null) continue;
                    if (cx >= hz.rect.left() && cx < hz.rect.right() && cy >= hz.rect.top() && cy < hz.rect.bottom()) {
                        if (hz.effects.contains(LevelBlueprint.HazardEffectType.DAMAGE_OVER_TIME)) {
                            player.hurt(1);
                        }
                        // SLOW effect would require modifying player speed; skip for now
                    }
                }
            }
        }

        // unlock puzzle doors
        for (java.util.AbstractMap.SimpleEntry<BossDoor, String> pl : puzzleLocks) {
            if (pl.getKey() != null && pl.getKey().isLocked() && isPuzzleSolved(pl.getValue())) {
                pl.getKey().unlock();
            }
        }
        updateCamera();
        this.processQueuedVisualObjectChanges();
    }

    public void render(Graphics2D g, int tickCount) {
        drawBG(g, tickCount);
        Graphics2D worldGraphics = (Graphics2D) g.create();
        try {
            worldGraphics.translate(-cameraX, -cameraY);
            for (int rowIndex : new int[] {0, 1, 2, 4}) {
                if (rowIndex >= levelVisualObjects.size()) continue;
                for (VisualObject obj : levelVisualObjects.get(rowIndex)) {
                    if (obj != null) obj.render(worldGraphics, tickCount);
                }
            }
        } finally {
            worldGraphics.dispose();
        }

        if (levelVisualObjects.size() > 3) {
            for (VisualObject obj : levelVisualObjects.get(3)) {
                if (obj != null) obj.render(g, tickCount);
            }
        }
        displayUI(g, tickCount);
        if (player != null && player.isInventoryOpen()) {
            inventoryPanel.draw(g, player, mouseX, mouseY, Viewport.WIDTH, Viewport.HEIGHT);
        }
    }

    private boolean checkInteractions(InputFrame input, int currentTick) {
        if (this.player == null) return false;
        boolean openedChest = false;

        for (ArrayList<VisualObject> row : this.levelVisualObjects) {
            for (VisualObject obj : row) {
                if (obj instanceof Checkpoint) {
                    ((Checkpoint) obj).checkActivation(this.player);
                } else if (obj instanceof Chest) {
                    Chest chest = (Chest) obj;
                    if (!input.isRightClick() && input.hasMousePress()) {
                        openedChest |= chest.tryOpen(this.player,
                                input.getClickXDown() + cameraX,
                                input.getClickYDown() + cameraY);
                    }
                } else if (obj instanceof PuzzleButton) {
                    PuzzleButton pb = (PuzzleButton) obj;
                    if (input.hasMousePress() && !input.isRightClick()) {
                        pb.tryActivate(this.player);
                    }
                } else if (obj instanceof KeyPickup) {
                    ((KeyPickup) obj).checkPickup(this.player);
                } else if (obj instanceof MazeExitPortal) {
                    ((MazeExitPortal) obj).check(this.player);
                } else if (obj instanceof DroppedItem) {
                    ((DroppedItem) obj).checkPickup(this.player);
                }
            }
        }

        boolean abilDisabled = isAbilitiesDisabled();
        boolean ultDisabled = isUltimatesDisabled();
        if (!abilDisabled) {
            if (input.wasPressed(81)) {
                this.player.tryActivateAbility(0, this, currentTick, mouseX + cameraX, mouseY + cameraY);
            }
            if (input.wasPressed(69)) {
                this.player.tryActivateAbility(1, this, currentTick, mouseX + cameraX, mouseY + cameraY);
            }
            if (input.wasPressed(70)) {
                this.player.tryActivateAbility(2, this, currentTick, mouseX + cameraX, mouseY + cameraY);
            }
        }
        if (!ultDisabled && input.wasPressed(82)) {
            this.player.tryActivateOrRetriggerUltimate(this, currentTick, mouseX + cameraX, mouseY + cameraY);
        }
        return openedChest;
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

        int currentW = player.getMaxHealth() <= 0 ? 0
                : (int) Math.max(0, ((double) player.getHealth() / player.getMaxHealth()) * barW);
        g2d.setColor(new Color(220, 40, 40));
        g2d.fillRoundRect(hx, hy, currentW, barH, 6, 6);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(hx, hy, barW, barH, 6, 6);

        // Rolls indicator pips
        int rollX = hx;
        int rollY = hy - 30;
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2d.setColor(new Color(200, 200, 220));
        g2d.drawString("Rolls [Space]:", rollX, rollY);
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
        abilityBarUI.draw(g, player.getAbilitySlots(), currentTick, Viewport.WIDTH, Viewport.HEIGHT);
        ultimateBarUI.draw(g, player.getUltimate(), player.getUltimateCharge(), Viewport.WIDTH, Viewport.HEIGHT);

        // 3. Hotbar (1-9)
        hotbarUI.draw(g, player.getInventory(), player.getMainHandIndex(), Viewport.WIDTH, Viewport.HEIGHT);

        drawHeldItemSummary(g2d, player, Viewport.WIDTH, Viewport.HEIGHT);

        // Inventory toggle hint
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2d.setColor(new Color(160, 160, 180));
        g2d.drawString("[I] Inventory", Viewport.WIDTH - 110, 28);
    }

    private void drawHeldItemSummary(Graphics2D g2d, Player player, int screenWidth, int screenHeight) {
        int panelWidth = 300;
        int panelHeight = 68;
        int x = screenWidth - panelWidth - 24;
        int y = screenHeight - panelHeight - 22;

        g2d.setColor(new Color(20, 20, 25, 210));
        g2d.fillRoundRect(x, y, panelWidth, panelHeight, 10, 10);
        g2d.setColor(new Color(70, 70, 85));
        g2d.drawRoundRect(x, y, panelWidth, panelHeight, 10, 10);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2d.setColor(Color.WHITE);

        Item heldItem = player.getEquippedItem();
        if (heldItem == null) {
            g2d.drawString("No item held", x + 12, y + 24);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2d.setColor(new Color(180, 185, 200));
            g2d.drawString("Select a hotbar item (1-9)", x + 12, y + 46);
            return;
        }

        g2d.drawString(heldItem.getName(), x + 12, y + 24);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2d.setColor(new Color(210, 215, 230));
        if (heldItem instanceof Consumable) {
            Consumable consumable = (Consumable) heldItem;
            g2d.drawString("Uses left: " + consumable.getUsesRemaining()
                    + "   Stack: " + consumable.getStackCount(), x + 12, y + 47);
        } else if (heldItem instanceof MeleeWeapon) {
            MeleeWeapon weapon = (MeleeWeapon) heldItem;
            g2d.drawString(String.format("DMG: %d   RNG: %d",
                    weapon.getDamage(), weapon.getAttackRange()), x + 12, y + 47);
        } else if (heldItem instanceof Wand) {
            Wand wand = (Wand) heldItem;
            g2d.drawString(String.format("DMG: %d   RNG: %d",
                    wand.getDamage(), wand.getEffectiveRange()), x + 12, y + 47);
        } else {
            g2d.drawString("Uses left: N/A   Stack: 1", x + 12, y + 47);
        }
    }

    public void drawBG(Graphics g, int tickCount) {
        if (this.backgroundImage != null) {
            g.drawImage(this.backgroundImage, this.bgX, this.bgY, null);
        } else {
            Color base = this.backgroundColor != null ? this.backgroundColor : new Color(30, 30, 35);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(base);
                g2.fillRect(0, 0, Viewport.WIDTH, Viewport.HEIGHT);
                // subtle vignette
                RadialGradientPaint vignette = new RadialGradientPaint(
                        new java.awt.geom.Point2D.Float(Viewport.WIDTH / 2f, Viewport.HEIGHT / 2f),
                        Math.max(Viewport.WIDTH, Viewport.HEIGHT) * 0.75f,
                        new float[]{0f, 1f},
                        new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 120)});
                g2.setPaint(vignette);
                g2.fillRect(0, 0, Viewport.WIDTH, Viewport.HEIGHT);
                // faint magical motes
                long seed = 1337L;
                for (int i = 0; i < 28; i++) {
                    long s = seed + i * 7919L;
                    int mx = (int)((s * 9301L + 49297L) % 233280L) % Viewport.WIDTH;
                    int my = (int)((s * 233L + 7L) % 233280L) % Viewport.HEIGHT;
                    double t = (tickCount + i * 17) * 0.03;
                    int ox = (int)(Math.sin(t) * 6);
                    int oy = (int)(Math.cos(t * 0.8) * 4);
                    int alpha = 18 + (int)(8 * Math.sin(t * 1.3));
                    g2.setColor(new Color(140, 120, 220, Math.max(6, alpha)));
                    g2.fillOval(mx + ox, my + oy, 2, 2);
                }
            } finally {
                g2.dispose();
            }
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

    public void keyHandler(InputFrame input, int tickCount) {
        if (player == null) return;

        Set<Integer> pressedKeys = input.getHeldKeys();
        int speed = (int) player.getSpeed();
        if (isPlayerInSlowHazard()) speed = Math.max(1, speed / 2);
        int moveX = 0;
        int moveY = 0;

        // Roll mechanic: Space (32)
        if (input.wasPressed(32) && player.rolls > 0) {
            int rollDist = isPlayerInSlowHazard() ? 20 : 40;
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

        setPlayerWorldPosition(finalX, finalY);
    }

    /** Move the player in world space and keep the camera clamped to the level. */
    public void movePlayerInWorld(int worldDx, int worldDy) {
        if (player == null || (worldDx == 0 && worldDy == 0)) return;
        int startX = player.getX();
        int startY = player.getY();
        int steps = Math.max(Math.abs(worldDx), Math.abs(worldDy));
        int finalX = startX;
        int finalY = startY;

        for (int step = 1; step <= steps; step++) {
            int candidateX = startX + (int) Math.round(worldDx * (step / (double) steps));
            int candidateY = startY + (int) Math.round(worldDy * (step / (double) steps));
            if (isPlayerBlockedAt(candidateX, candidateY)) break;
            finalX = candidateX;
            finalY = candidateY;
        }

        setPlayerWorldPosition(finalX, finalY);
    }

    private void setPlayerWorldPosition(int x, int y) {
        player.setX(x);
        player.setY(y);
        player.getHitbox().setRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        updateCamera();
    }

    private boolean isPlayerBlockedAt(int x, int y) {
        if (width > 0 && height > 0
                && (x < 0 || y < 0 || x + player.getWidth() > width || y + player.getHeight() > height)) {
            return true;
        }

        for (VisualObject object : levelVisualObjects.get(1)) {
            if (!(object instanceof Border)) continue;
            if (object instanceof RallyBarricade) continue;
            if (object instanceof BossDoor) {
                BossDoor bd = (BossDoor) object;
                if (!bd.isLocked()) continue;
                // check item key locks
                boolean unlockedByKey = false;
                for (java.util.AbstractMap.SimpleEntry<BossDoor, String> ik : itemKeyLocks) {
                    if (ik.getKey() == bd && player != null && player.hasKey(ik.getValue())) {
                        player.consumeKey(ik.getValue());
                        bd.unlock();
                        unlockedByKey = true;
                        break;
                    }
                }
                if (unlockedByKey) continue;
            } else {
                // not a BossDoor, but still could be other blocking border types? No other special cases now
            }

            Border border = (Border) object;
            if (x < border.getX() + border.getWidth()
                    && x + player.getWidth() > border.getX()
                    && y < border.getY() + border.getHeight()
                    && y + player.getHeight() > border.getY()) {
                return true;
            }
        }
        return false;
    }

    public void mouseHandler(int clickXDown, int clickYDown, int currentTick) {
        if (clickXDown == -1 || clickYDown == -1 || player == null) return;
        if (isWeaponAttackDisabled()) return;
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

    private void updateCamera() {
        if (player == null) return;
        int centerX = player.getX() + player.getWidth() / 2;
        int centerY = player.getY() + player.getHeight() / 2;
        cameraX = clamp(centerX - Viewport.WIDTH / 2, 0, Math.max(0, width - Viewport.WIDTH));
        cameraY = clamp(centerY - Viewport.HEIGHT / 2, 0, Math.max(0, height - Viewport.HEIGHT));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
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

    public void setRoomRegions(List<LevelBlueprint.RoomRegionSpec> regions) {
        roomRegions.clear();
        if (regions != null) roomRegions.addAll(regions);
    }

    public void setHazardZones(List<LevelBlueprint.HazardZoneSpec> zones) {
        hazardZones.clear();
        if (zones != null) hazardZones.addAll(zones);
    }

    private boolean isPlayerInSlowHazard() {
        if (player == null) return false;
        int cx = player.getX() + player.getWidth() / 2;
        int cy = player.getY() + player.getHeight() / 2;
        for (LevelBlueprint.HazardZoneSpec hz : hazardZones) {
            if (hz == null || hz.rect == null) continue;
            if (cx >= hz.rect.left() && cx < hz.rect.right() && cy >= hz.rect.top() && cy < hz.rect.bottom()) {
                if (hz.effects.contains(LevelBlueprint.HazardEffectType.SLOW)) return true;
            }
        }
        return false;
    }

    public void setPuzzleSolved(String id) {
        if (id != null && !id.isEmpty()) puzzleSolved.put(id, true);
    }

    public boolean isPuzzleSolved(String id) {
        return id != null && Boolean.TRUE.equals(puzzleSolved.get(id));
    }

    public void addPuzzleLock(BossDoor door, String puzzleId) {
        if (door == null || puzzleId == null) return;
        puzzleLocks.add(new java.util.AbstractMap.SimpleEntry<>(door, puzzleId));
        if (isPuzzleSolved(puzzleId) && door.isLocked()) door.unlock();
    }

    public void addItemKeyLock(BossDoor door, String keyId) {
        if (door == null || keyId == null) return;
        itemKeyLocks.add(new java.util.AbstractMap.SimpleEntry<>(door, keyId));
    }

    public RoomRestriction getActiveRestrictionForPlayer() {
        if (player == null) return RoomRestriction.NONE;
        int cx = player.getX() + player.getWidth() / 2;
        int cy = player.getY() + player.getHeight() / 2;
        boolean w = false, a = false, u = false;
        for (LevelBlueprint.RoomRegionSpec r : roomRegions) {
            if (r == null || r.rect == null || r.restriction == null) continue;
            if (cx >= r.rect.left() && cx < r.rect.right() && cy >= r.rect.top() && cy < r.rect.bottom()) {
                if (r.restriction.disableWeapons) w = true;
                if (r.restriction.disableAbilities) a = true;
                if (r.restriction.disableUltimates) u = true;
            }
        }
        for (LevelBlueprint.HazardZoneSpec hz : hazardZones) {
            if (hz == null || hz.rect == null) continue;
            if (cx >= hz.rect.left() && cx < hz.rect.right() && cy >= hz.rect.top() && cy < hz.rect.bottom()) {
                if (hz.disableWeapons) w = true;
                if (hz.disableAbilitiesUlt) { a = true; u = true; }
            }
        }
        if (!w && !a && !u) return RoomRestriction.NONE;
        return new RoomRestriction(w, a, u);
    }

    public boolean isWeaponAttackDisabled() {
        return getActiveRestrictionForPlayer().disableWeapons;
    }

    public boolean isAbilitiesDisabled() {
        return getActiveRestrictionForPlayer().disableAbilities;
    }

    public boolean isUltimatesDisabled() {
        return getActiveRestrictionForPlayer().disableUltimates;
    }

    public InventoryPanel getInventoryPanel() {
        return inventoryPanel;
    }

    public Player getPlayer() {
        return player;
    }

    public int getCameraX() {
        return cameraX;
    }

    public int getCameraY() {
        return cameraY;
    }

    /** Clips a dash/teleport path so the player cannot pass through blocking walls or out of bounds. */
    public int[] clipDashPath(int startCenterX, int startCenterY, int endCenterX, int endCenterY, int playerWidth, int playerHeight) {
        if (player == null) return new int[]{endCenterX, endCenterY};
        int startX = startCenterX - playerWidth / 2;
        int startY = startCenterY - playerHeight / 2;
        int endX = endCenterX - playerWidth / 2;
        int endY = endCenterY - playerHeight / 2;
        int dx = endX - startX;
        int dy = endY - startY;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        int lastX = startX;
        int lastY = startY;
        if (steps == 0) return new int[]{startCenterX, startCenterY};
        for (int step = 1; step <= steps; step++) {
            int cx = startX + (int) Math.round(dx * (step / (double) steps));
            int cy = startY + (int) Math.round(dy * (step / (double) steps));
            if (isPlayerBlockedAt(cx, cy)) break;
            lastX = cx;
            lastY = cy;
        }
        return new int[]{lastX + playerWidth / 2, lastY + playerHeight / 2};
    }
}
