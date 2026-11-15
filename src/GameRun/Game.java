package GameRun;

import Levels.BaseLevel;
import Levels.Level1;
import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import ShapeCore.DeathOverlay;
import ShapeCore.Buttons.Button;
import ShapeCore.Buttons.LevelSelectButton;
import ShapeCore.Buttons.LevelSelectorButton;
import GameRun.WinScreen;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import Objects.PlayerEquipment.Item;
import Objects.PlayerEquipment.Ultimate;

public class Game {
    private int tickCount;
    protected GameStates state = GameStates.INITIALIZED_0;
    public final ArrayList<VisualObject> visualObjects = new ArrayList<>();
    public Player player;
    private final ArrayList<VisualObject> toAdd = new ArrayList<>();
    private final ArrayList<VisualObject> toRemove = new ArrayList<>();
    private BaseLevel currentLevel;
    private final DeathOverlay deathOverlay;
    private WinScreen winScreen;
    private final ProgressionStore progressionStore;
    private RunRoute runRoute;
    private final RouteMapScreen routeMapScreen = new RouteMapScreen(this);
    private final ShopScreen shopScreen = new ShopScreen(this);
    private final List<LevelDefinition> levelDefinitions = Arrays.asList(
            LevelDefinition.playable("level-1", "Level 1 - Tutorial",
                    player -> new Level1(0, 0, player)),
            LevelDefinition.unavailable("level-2", "Level 2"));
    private String selectedLevelId = "level-1";

    public Game() {
        this(new ProgressionStore());
    }

    Game(ProgressionStore progressionStore) {
        this.progressionStore = Objects.requireNonNull(progressionStore, "progressionStore");
        this.progressionStore.importLegacyLevelSelectorUnlock();
        this.deathOverlay = new DeathOverlay(this);
        changeState(GameStates.INITIALIZED_1);
        applyQueuedVisualObjectChanges();
    }

    public GameStates getState() {
        return state;
    }

    public int getTickCount() {
        return tickCount;
    }

    public BaseLevel getCurrentLevel() {
        return currentLevel;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isLevelSelectorUnlocked() {
        return progressionStore.isLevelSelectorUnlocked();
    }

    public void openLevelSelector() {
        if (!isLevelSelectorUnlocked()) {
            throw new IllegalStateException("The level selector is not unlocked.");
        }
        changeState(GameStates.LEVEL_SELECT);
    }

    public void chooseLevel(String levelId) {
        if (state != GameStates.LEVEL_SELECT) {
            throw new IllegalStateException("Choose a level from the level selector.");
        }
        for (LevelDefinition definition : levelDefinitions) {
            if (definition.getId().equals(levelId)) {
                if (!definition.isAvailable()) {
                    throw new IllegalStateException("Level is not available: " + levelId);
                }
                selectedLevelId = levelId;
                changeState(GameStates.GAME_STARTED_0);
                return;
            }
        }
        throw new IllegalArgumentException("Unknown level: " + levelId);
    }

    public void startSelectedLevel() {
        if (player == null) {
            throw new IllegalStateException("Choose a hero before starting a level.");
        }
        Map<String, Object> savedRun = progressionStore.getRunState(player);
        runRoute = RunRoute.fromData(savedRun);
        if (runRoute == null) {
            runRoute = RunRoute.generate(RunRoute.DEFAULT_STAGES, System.nanoTime());
            progressionStore.saveRunState(player, runRoute.toData());
        } else if (runRoute.getPendingNode() != null
                && runRoute.getPendingNode().type != RunRoute.Type.SHOP) {
            runRoute.interrupted();
            progressionStore.saveRunState(player, runRoute.toData());
        }
        currentLevel = null;
        if (runRoute.getPendingNode() != null && runRoute.getPendingNode().type == RunRoute.Type.SHOP) {
            shopScreen.ensureStock(runRoute.getPendingNode());
            changeState(GameStates.SHOP);
        } else {
            changeState(GameStates.ROUTE_MAP);
        }
    }

    public void selectPlayer(Player selectedPlayer) {
        this.player = Objects.requireNonNull(selectedPlayer, "selectedPlayer");
        progressionStore.restorePlayer(this.player);
    }

    public void savePlayerProgress() {
        if (player != null) {
            if (currentLevel != null) progressionStore.savePlayer(player, currentLevel.getStoredItems());
            else progressionStore.savePlayer(player);
        }
    }

    public void savePlayerInventory() {
        savePlayerProgress();
    }

    public int getPlayerCoins() {
        return player == null ? 0 : progressionStore.getCoins(player);
    }

    public int addPlayerCoins(int amount) {
        if (player == null) throw new IllegalStateException("Choose a hero before earning coins.");
        return progressionStore.addCoins(player, amount);
    }

    public RunRoute getRunRoute() { return runRoute; }

    public boolean ownsUltimate(String id) {
        return player != null && progressionStore.ownsUltimate(player, id);
    }

    public void enterRouteNode(String nodeId) {
        if (state != GameStates.ROUTE_MAP || runRoute == null || !runRoute.enter(nodeId)) return;
        RunRoute.Node node = runRoute.getPendingNode();
        progressionStore.saveRunState(player, runRoute.toData());
        currentLevel = null;
        if (node.type == RunRoute.Type.SHOP) {
            shopScreen.ensureStock(node);
            progressionStore.saveRunState(player, runRoute.toData());
            changeState(GameStates.SHOP);
        } else {
            changeState(GameStates.LEVEL_1);
        }
    }

    public void buyShopOffer(RunRoute.Node node, Map<String, Object> offer) {
        if (node == null || offer == null || state != GameStates.SHOP || runRoute == null
                || node != runRoute.getPendingNode()) return;
        String offerId = String.valueOf(offer.get("offerId"));
        if (node.purchased.contains(offerId)) return;
        int price = offer.get("price") instanceof Number ? ((Number) offer.get("price")).intValue() : Integer.MAX_VALUE;
        boolean bought = false;
        if ("ultimate".equals(offer.get("type"))) {
            Ultimate power = Ultimate.forId(offer.get("id") instanceof String ? (String) offer.get("id") : null);
            if (power != null) bought = progressionStore.purchaseUltimate(player, power.getType(), price);
        } else {
            Item item = ProgressionStore.deserializeShopItem(offer);
            if (item != null) bought = progressionStore.purchaseItem(player, item, price);
        }
        if (bought) {
            node.purchased.add(offerId);
            progressionStore.savePlayer(player, progressionStore.getStoredItems(player));
            progressionStore.saveRunState(player, runRoute.toData());
        }
    }

    public void leaveShop(RunRoute.Node node) {
        if (state != GameStates.SHOP || runRoute == null || node != runRoute.getPendingNode()) return;
        runRoute.completePendingNode();
        progressionStore.saveRunState(player, runRoute.toData());
        changeState(GameStates.ROUTE_MAP);
    }

    public void continueAfterReward(Item item) {
        if (item != null && player != null) {
            if (!player.addToHotbar(item) && currentLevel != null) currentLevel.collectItem(item);
        }
        savePlayerProgress();
        RunRoute.Node completed = runRoute == null ? null : runRoute.getPendingNode();
        if (completed != null && completed.type == RunRoute.Type.BOSS) {
            progressionStore.clearRunState(player);
            runRoute = null;
            currentLevel = null;
            changeState(GameStates.INITIALIZED_1);
            return;
        }
        if (completed != null) runRoute.completePendingNode();
        currentLevel = null;
        progressionStore.saveRunState(player, runRoute == null ? null : runRoute.toData());
        changeState(GameStates.ROUTE_MAP);
    }

    public void endRunAfterDeath() {
        if (player != null) {
            savePlayerProgress();
            progressionStore.clearRunState(player);
        }
        runRoute = null;
        currentLevel = null;
        changeState(GameStates.INITIALIZED_1);
    }

    public void update(InputFrame input) {
        tickCount++;

        for (int i = visualObjects.size() - 1; i >= 0; i--) {
            VisualObject object = visualObjects.get(i);
            if (object instanceof Button) {
                ((Button) object).update(input, tickCount);
            } else {
                object.update(tickCount);
            }
        }
        applyQueuedVisualObjectChanges();

        if (state == GameStates.LEVEL_1 && currentLevel != null) {
            currentLevel.update(input, tickCount);
            if (player != null && player.isDead()) {
                savePlayerProgress();
                progressionStore.clearRunState(player);
                runRoute = null;
                changeState(GameStates.PLAYER_DEAD);
            } else if (currentLevel.isComplete()) {
                RunRoute.Node completed = runRoute == null ? null : runRoute.getPendingNode();
                if (completed != null) {
                    String levelId = completed.type == RunRoute.Type.TUTORIAL ? "level-1" : completed.id;
                    progressionStore.completeLevel(player, levelId);
                }
                changeState(GameStates.WIN_SCREEN);
            }
        }
        if (state == GameStates.ROUTE_MAP) routeMapScreen.update(input);
        if (state == GameStates.SHOP && runRoute != null && runRoute.getPendingNode() != null) {
            shopScreen.update(input, runRoute.getPendingNode());
        }
        if (state == GameStates.PLAYER_DEAD) {
            deathOverlay.update(input, tickCount);
        }
        if (state == GameStates.WIN_SCREEN && winScreen != null) {
            winScreen.update(input, tickCount);
            if (winScreen.isRewardClaimed() && input.wasPressed(10)) { // ENTER key
                winScreen.handleContinue();
            }
        }
    }

    public void render(Graphics2D g) {
        g.setColor(backgroundColor());
        g.fillRect(0, 0, Viewport.WIDTH, Viewport.HEIGHT);

        switch (state) {
            case INITIALIZED_1:
                g.setColor(Color.WHITE);
                g.setFont(new Font("Serif", Font.BOLD, 48));
                g.drawString("DUNGEON CRAWLER", 700, 250);
                renderMenuObjects(g);
                break;
            case GAME_STARTED_0:
                g.setColor(Color.WHITE);
                g.setFont(new Font("Serif", Font.BOLD, 40));
                g.drawString("CHOOSE YOUR HERO", 730, 250);
                renderMenuObjects(g);
                break;
            case LEVEL_SELECT:
                g.setColor(Color.WHITE);
                g.setFont(new Font("Serif", Font.BOLD, 40));
                g.drawString("SELECT A LEVEL", 760, 250);
                renderMenuObjects(g);
                break;
            case ROUTE_MAP:
                routeMapScreen.render(g);
                break;
            case SHOP:
                if (runRoute != null && runRoute.getPendingNode() != null) shopScreen.render(g, runRoute.getPendingNode());
                break;
            case LEVEL_1:
                if (currentLevel != null) currentLevel.render(g, tickCount);
                break;
            case WIN_SCREEN:
                if (winScreen != null) winScreen.render(g, tickCount);
                break;
            case PLAYER_DEAD:
                deathOverlay.render(g);
                break;
            default:
                break;
        }
    }

    private void renderMenuObjects(Graphics2D g) {
        for (VisualObject object : visualObjects) {
            object.render(g, tickCount);
        }
    }

    private Color backgroundColor() {
        switch (state) {
            case INITIALIZED_1:
                return new Color(30, 32, 40);
            case GAME_STARTED_0:
                return new Color(25, 28, 36);
            case LEVEL_SELECT:
                return new Color(25, 28, 36);
            case ROUTE_MAP:
                return new Color(18, 20, 29);
            case SHOP:
                return new Color(24, 22, 25);
            case LEVEL_1:
                return new Color(20, 20, 20);
            case WIN_SCREEN:
                return new Color(20, 25, 35);
            case PLAYER_DEAD:
                return new Color(20, 0, 0);
            default:
                return Color.BLACK;
        }
    }

    private void applyQueuedVisualObjectChanges() {
        visualObjects.removeAll(toRemove);
        toRemove.clear();
        visualObjects.addAll(toAdd);
        toAdd.clear();
    }

    public void changeState(GameStates newState) {
        if (newState == GameStates.LEVEL_SELECT && !isLevelSelectorUnlocked()) {
            throw new IllegalStateException("The level selector is not unlocked.");
        }
        if (newState == GameStates.INITIALIZED_1 && state != GameStates.INITIALIZED_1) {
            savePlayerProgress();
            this.currentLevel = null;
        }
        if (newState == GameStates.GAME_STARTED_0 && player != null) savePlayerProgress();
        this.state = newState;
        this.toRemove.addAll(this.visualObjects);

        switch (this.state) {
            case INITIALIZED_1:
                this.toAdd.add(new ShapeCore.Buttons.StartButton(800, 410, 300, 80, this));
                break;
            case GAME_STARTED_0:
                this.toAdd.add(new ShapeCore.Buttons.ClassSelectionButtons.RogueButton(600, 400, 180, 80, this));
                this.toAdd.add(new ShapeCore.Buttons.ClassSelectionButtons.TankButton(860, 400, 180, 80, this));
                this.toAdd.add(new ShapeCore.Buttons.ClassSelectionButtons.WizardButton(1120, 400, 180, 80, this));
                break;
            case LEVEL_SELECT:
                for (int i = 0; i < levelDefinitions.size(); i++) {
                    this.toAdd.add(new LevelSelectButton(
                            800, 360 + i * 100, 300, 70, levelDefinitions.get(i), this));
                }
                this.toAdd.add(new Button(800, 590, 300, 60, "Back to Menu",
                        new Color(90, 90, 95), this) {
                    @Override
                    public void buttonAction() {
                        changeState(GameStates.INITIALIZED_1);
                    }
                });
                break;
            case LEVEL_1:
                if (this.currentLevel == null || this.player == null || this.player.checkpointX < 0) {
                    RunRoute.Node node = runRoute == null ? null : runRoute.getPendingNode();
                    if (node == null) throw new IllegalStateException("No route node is selected.");
                    if (node.type == RunRoute.Type.TUTORIAL) this.currentLevel = new Level1(0, 0, player);
                    else if (node.type == RunRoute.Type.BOSS) this.currentLevel = new Levels.RunBossRoom(player, runRoute.getStageCount());
                    else this.currentLevel = new Levels.RunCombatRoom(player, node.stage, runRoute.getSeed() ^ node.id.hashCode());
                    this.currentLevel.setProgressSaveListener(this::savePlayerProgress);
                    this.currentLevel.setCoinWallet(this::getPlayerCoins, this::addPlayerCoins);
                    progressionStore.restorePlayer(this.player);
                    this.currentLevel.setStoredItems(progressionStore.getStoredItems(this.player));
                    this.currentLevel.setProgressSaveListener(this::savePlayerProgress);
                    this.currentLevel.setCoinWallet(this::getPlayerCoins, this::addPlayerCoins);
                } else {
                    this.player.respawnAtCheckpoint(400, 300);
                }
                break;
            case WIN_SCREEN:
                this.winScreen = new WinScreen(this);
                break;
            case PLAYER_DEAD:
                break;
            default:
                break;
        }
    }

    private BaseLevel createSelectedLevel() {
        for (LevelDefinition definition : levelDefinitions) {
            if (definition.getId().equals(selectedLevelId)) {
                return definition.create(player);
            }
        }
        throw new IllegalStateException("Selected level is not registered: " + selectedLevelId);
    }
}
