package GameRun;

import Objects.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

public class Game {

    private int tickCount = 0;
    protected String state = "";
    public ArrayList<VisualObject> visualObjects = new ArrayList<>(); // Example array of visual objects

    public Game() {
        this.state = "Initialized.0";
        System.out.println(this.state);
        this.changeState("Initialized.1");
        System.out.println(this.state);
    }

    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp) {
        this.tickCount++;
        for (var i = this.visualObjects.size() - 1; i >= 0; i--) {
            this.visualObjects.get(i).tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, this.tickCount);
        }
    }
    

    public void screenUpdate(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp) {
        switch (this.state) {
            case "Initialized.1":
                g.setColor(Color.GRAY);
                // g.fillRect(0, 0, 1500, 1300);
                break;
            default:
                g.drawString("Unknown state: " + this.state, 50, 50);
        }
        this.tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp);
    }

    public void changeState(String newState) {
        switch (this.state) {
            case "Initialized.0":
                if (!newState.equals("Initialized.1")) {
                    System.out.println("Invalid state transition from Initialized.0 to " + newState);
                    return;
                }
                break;
            case "Initialized.1":

                break;
            default:
                System.out.println("State not recognized: " + this.state);
        }
        this.state = newState;
        switch (this.state) {
            case "Initialized.1":
                this.visualObjects.add(new Objects.Buttons.StartButton(50, 50, 100, 50, this));
                System.out.println("Start Button added");

                break;
            default:
                System.out.println("State not recognized: " + this.state);
        }
    }
}
