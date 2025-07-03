package GameRun;

import Levels.*;
import Objects.*;
import Objects.PlayerClasses.Player;
import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

public class Game {

    private int tickCount = 0;
    protected GameStates state = GameStates.INITIALIZED_0;
    public ArrayList<VisualObject> visualObjects = new ArrayList<>(); // Example array of visual objects
    public Player player;

    private final ArrayList<VisualObject> toAdd = new ArrayList<>();
    private final ArrayList<VisualObject> toRemove = new ArrayList<>();

    private BaseLevel currentLevel;

    public Game() {
        this.state = GameStates.INITIALIZED_0;
        this.changeState(GameStates.INITIALIZED_1);
        // Immediately process any objects added by changeState
        this.visualObjects.addAll(this.toAdd);
        this.toAdd.clear();
    }

    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp) {
        this.tickCount++;
        if (visualObjects.isEmpty()) {
            // System.out.println("No visual objects to tick.");
            return;
        }
        for (int i = this.visualObjects.size() - 1; i >= 0; i--) {
            this.visualObjects.get(i).tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, this.tickCount);
            // System.out.println("Ticking object: " + this.visualObjects.get(i).getClass().getSimpleName());
            // System.out.println("# of visual objects: " + this.visualObjects.size());
        }
        visualObjects.removeAll(this.toRemove);
        this.toRemove.clear();

        visualObjects.addAll(this.toAdd);
        this.toAdd.clear();
        if (this.currentLevel != null){
            this.currentLevel.tick(g);
        }
    }

    private void bgRect(Graphics g) {
        g.fillRect(0, 0, 2000, 1300);
    }

    public void screenUpdate(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp) {
        switch (this.state) {
            case INITIALIZED_1:
                g.setColor(Color.GRAY);
                this.bgRect(g);
                break;
            case GAME_STARTED_0:
                g.setColor(Color.LIGHT_GRAY);
                this.bgRect(g);
                break;
            case LEVEL_1:
                g.setColor(Color.DARK_GRAY);
                this.bgRect(g);
                break;
            default:
                System.out.println("Unknown state: " + this.state);

        }
        this.tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp);
    }

    public void changeState(GameStates newState) {
        //switch from logic
        switch (this.state) {
            case INITIALIZED_0:
                if (newState != GameStates.INITIALIZED_1) {
                    System.out.println("Invalid state transition from Initialized.0 to " + newState);
                    return;
                }
                break;
            case INITIALIZED_1:
                break;

            case GAME_STARTED_0:
                break;

            case LEVEL_1:
                break;

            default:
                System.out.println("State not recognized (switch from): " + this.state);
        }
        this.state = newState;
        switch (this.state) {
            case INITIALIZED_1:
                this.toAdd.add(new Objects.Buttons.StartButton(50, 50, 200, 100, this));
                // System.out.println("Start Button added");

                break;

            case GAME_STARTED_0:
                this.toRemove.addAll(this.visualObjects);

                this.toAdd.add(new Objects.Buttons.ClassSelectionButtons.RogueButton(200, 400, 100, 50, this));
                this.toAdd.add(new Objects.Buttons.ClassSelectionButtons.WizardButton(400, 400, 100, 50, this));
                this.toAdd.add(new Objects.Buttons.ClassSelectionButtons.TankButton(600, 400, 100, 50, this));

                break;
            case LEVEL_1:
                this.toRemove.addAll(this.visualObjects);
                
                
                break;
            default:
                System.out.println("State not recognized (switch to): " + this.state);
        }
    }
}
