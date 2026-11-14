package GameRun;

import Levels.*;
import Objects.*;
import Objects.PlayerClasses.Player;
import ShapeCore.DeathOverlay;
import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

public class Game {

    private int tickCount = 0;
    protected GameStates state = GameStates.INITIALIZED_0;
    public ArrayList<VisualObject> visualObjects = new ArrayList<>();
    public Player player;
    private final ArrayList<VisualObject> toAdd = new ArrayList<>();
    private final ArrayList<VisualObject> toRemove = new ArrayList<>();

    private BaseLevel currentLevel;
    private DeathOverlay deathOverlay;

    private int mouseX = 0;
    private int mouseY = 0;
    private boolean isRightClick = false;

    public Game() {
        this.deathOverlay = new DeathOverlay(this);
        this.state = GameStates.INITIALIZED_0;
        this.changeState(GameStates.INITIALIZED_1);
        this.visualObjects.addAll(this.toAdd);
        this.toAdd.clear();
    }

    public void setMousePosition(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    public void setRightClick(boolean rightClick) {
        this.isRightClick = rightClick;
    }

    public boolean isRightClick() { return isRightClick; }
    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
    public GameStates getState() { return state; }

    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int scrollDelta) {
        this.tickCount++;

        // Process menu / overlay visual objects
        for (int i = this.visualObjects.size() - 1; i >= 0; i--) {
            if (i < this.visualObjects.size()) {
                this.visualObjects.get(i).tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, this.tickCount);
            }
        }
        visualObjects.removeAll(this.toRemove);
        this.toRemove.clear();

        visualObjects.addAll(this.toAdd);
        this.toAdd.clear();

        // Level tick
        if (this.state == GameStates.LEVEL_1 && this.currentLevel != null) {
            this.currentLevel.setMousePosition(this.mouseX, this.mouseY);
            this.currentLevel.setRightClick(this.isRightClick);
            this.currentLevel.tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, this.tickCount, scrollDelta);

            // Check if player died during level tick
            if (this.player != null && this.player.isDead()) {
                changeState(GameStates.PLAYER_DEAD);
            }
        } else if (this.state == GameStates.PLAYER_DEAD) {
            deathOverlay.draw(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, this.tickCount, 1900, 1000);
        }
    }

    private void bgRect(Graphics g) {
        g.fillRect(0, 0, 1900, 1300);
    }

    public void screenUpdate(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int scrollDelta) {
        switch (this.state) {
            case INITIALIZED_1:
                g.setColor(new Color(30, 32, 40));
                this.bgRect(g);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Serif", Font.BOLD, 48));
                g.drawString("DUNGEON CRAWLER", 700, 250);
                break;
            case GAME_STARTED_0:
                g.setColor(new Color(25, 28, 36));
                this.bgRect(g);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Serif", Font.BOLD, 40));
                g.drawString("CHOOSE YOUR HERO", 730, 250);
                break;
            case LEVEL_1:
                g.setColor(new Color(20, 20, 20));
                this.bgRect(g);
                break;
            case PLAYER_DEAD:
                // Background handled in deathOverlay
                break;
            default:
                break;
        }
        this.tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, scrollDelta);
    }

    public void changeState(GameStates newState) {
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

            case LEVEL_1:
                if (this.currentLevel == null || this.player == null || this.player.checkpointX < 0) {
                    this.currentLevel = new Level1(0, 0, this.player);
                } else {
                    // Respawned in existing level
                    this.player.respawnAtCheckpoint(400, 300);
                }
                break;

            case PLAYER_DEAD:
                // Handled in tick via deathOverlay
                break;

            default:
                break;
        }
    }
}
