package ShapeCore.Buttons;

import GameRun.Game;
import GameRun.InputFrame;
import Objects.VisualObject;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
public class Button extends VisualObject {
    private String label;
    private Color color;
    protected Game game;
    protected Boolean notCurrentlyHeld = true;
    public Button(int x, int y, int width, int height, String label, Color color, Game game) {
        super(x, y, width, height, color);
        this.game = game;
        this.label = label;
        this.color = color;
        this.moveable = false; // Buttons are not moveable by default
    }
    @Override
    public void draw(Graphics g) {
        if (this.visible) {
            // Draw the button rectangle
            g.setColor(this.color);
            g.fillRect(this.x, this.y, this.width, this.height);
            // Draw the label text
            g.setColor(java.awt.Color.BLACK);
            g.drawString(this.label, this.x + 10, this.y + this.height / 2);
        }
    }

    public boolean isClicked(int clickX, int clickY) {
        return this.checkCollision(clickX, clickY);
    }

    public void update(InputFrame input, int tickCount) {
        int clickXDown = input.getClickXDown();
        int clickYDown = input.getClickYDown();
        int clickXUp = input.getClickXUp();
        int clickYUp = input.getClickYUp();
        if (input.hasMousePress() && this.isClicked(clickXDown, clickYDown)) {
            if (this.notCurrentlyHeld) {
                this.buttonAction();
                this.held = true;
                this.notCurrentlyHeld = false;
                // System.out.println("Button " + this.label + " clicked." );
            }
        } else if (clickXUp != -1 && clickYUp != -1) {
            this.held = false;
            this.notCurrentlyHeld = true;
            // System.out.println("Button " + this.label + " released." );
        }

    }

    @Override
    public void render(Graphics2D g, int tickCount) {
        this.draw(g);
    }
    // Child classes override
    public void buttonAction() {
        
    }
}
