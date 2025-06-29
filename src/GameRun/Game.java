package GameRun;

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

    public Game() {
        this.state = GameStates.INITIALIZED_0;
        this.changeState(GameStates.INITIALIZED_1);
    }

    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp) {
        this.tickCount++;
        for (var i = this.visualObjects.size() - 1; i >= 0; i--) {
            this.visualObjects.get(i).tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, this.tickCount);
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
                this.visualObjects.add(new Objects.Buttons.StartButton(50, 50, 200, 100, this));
                // System.out.println("Start Button added");

                break;

            case GAME_STARTED_0:
                this.visualObjects.clear();
                this.visualObjects.add(new Objects.Buttons.ClassSelectionButtons.RogueButton(200, 400, 100, 50, this));
                this.visualObjects.add(new Objects.Buttons.ClassSelectionButtons.WizardButton(400, 400, 100, 50, this));
                this.visualObjects.add(new Objects.Buttons.ClassSelectionButtons.TankButton(600, 400, 100, 50, this));

                break;
            case LEVEL_1:
                this.visualObjects.clear();
                this.visualObjects.add(this.player);
                this.player.scaleImage(75, 75);
                break;
            default:
                System.out.println("State not recognized (switch to): " + this.state);
        }
    }
}
