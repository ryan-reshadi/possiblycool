package GameRun;

import Levels.BaseLevel;
import Levels.Level1;
import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import ShapeCore.DeathOverlay;
import ShapeCore.Buttons.Button;
import ShapeCore.Buttons.LevelSelectButton;
import ShapeCore.Buttons.LevelSelectorButton;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Game {
    private int tickCount;
    protected GameStates state = GameStates.INITIALIZED_0;
    public final ArrayList<VisualObject> visualObjects = new ArrayList<>();
    public Player player;
    private final ArrayList<VisualObject> toAdd = new ArrayList<>();
    private final ArrayList<VisualObject> toRemove = new ArrayList<>();
    private BaseLevel currentLevel;
    private final DeathOverlay deathOverlay;
    private final ProgressionStore progressionStore;
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
        currentLevel = null;
        changeState(GameStates.LEVEL_1);
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
                changeState(GameStates.PLAYER_DEAD);
            } else if (currentLevel.isComplete()) {
                progressionStore.unlockLevelSelector();
                changeState(GameStates.INITIALIZED_1);
            }
        }
        if (state == GameStates.PLAYER_DEAD) {
            deathOverlay.update(input, tickCount);
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
            case LEVEL_1:
                if (currentLevel != null) currentLevel.render(g, tickCount);
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
            case LEVEL_1:
                return new Color(20, 20, 20);
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
        this.state = newState;
        this.toRemove.addAll(this.visualObjects);

        switch (this.state) {
            case INITIALIZED_1:
                this.toAdd.add(new ShapeCore.Buttons.StartButton(800, 410, 300, 80, this));
                if (isLevelSelectorUnlocked()) {
                    this.toAdd.add(new LevelSelectorButton(800, 510, 300, 80, this));
                }
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
                    this.currentLevel = createSelectedLevel();
                } else {
                    this.player.respawnAtCheckpoint(400, 300);
                }
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
