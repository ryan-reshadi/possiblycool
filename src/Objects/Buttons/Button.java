package Objects.Buttons;

import GameRun.Game;
import Objects.VisualObject;
import java.awt.Graphics;
import java.util.Set;
public class Button extends VisualObject {
    private String label, color;
    protected Game game;
    protected Boolean notCurrentlyHeld = true;
    public Button(int x, int y, int width, int height, String label, String color, Game game) {
        super(x, y, width, height, null);
        this.game = game;
        this.label = label;
        this.color = color;
    }
    @Override
    public void draw(Graphics g) {
        if (this.visible) {
            // Draw the button rectangle
            g.setColor(java.awt.Color.decode(color));
            g.fillRect(this.x, this.y, this.width, this.height);
            // Draw the label text
            g.setColor(java.awt.Color.BLACK);
            g.drawString(this.label, this.x + 10, this.y + this.height / 2);
        }
    }

    public boolean isClicked(int clickX, int clickY) {
        return this.checkCollision(clickX, clickY);
    }
    
    @Override
    public void baseTickFunc(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount){
        if (this.isClicked(clickXDown, clickYDown)) {
            if (this.notCurrentlyHeld) {
                this.buttonAction();
                this.held = true;
                this.notCurrentlyHeld = false;
            }
        } else if (clickXUp != -1 && clickYUp != -1) {
            this.held = false;
            this.notCurrentlyHeld = true;
        }

    }
    // Child classes override
    public void buttonAction() {
        
    }
}


