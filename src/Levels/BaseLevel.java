package Levels;

import Objects.*;
import Objects.PlayerClasses.Player;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class BaseLevel {

    protected int width;
    protected int height;
    protected ArrayList<ArrayList<VisualObject>> levelVisualObjects = new ArrayList<>();
    protected Image backgroundImage;
    protected int bgX, bgY;
    // protected ArrayList<Player> players;
    protected Player player;
    protected int buffer= 50;
    protected int offsetX = 0;
    protected int offsetY = 0;
    public BaseLevel(int bgX, int bgY, String path, ArrayList<Player> players) {
        this.bgX = bgX;
        this.bgY = bgY;
        this.levelVisualObjects = new ArrayList<>();
        this.levelVisualObjects.add(new ArrayList<>(players)); // Initialize with one empty row
        this.player = players.get(0); // Assuming the first player is the main player
        try {
            this.backgroundImage = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println("Image could not be loaded.");
            e.printStackTrace();
        }
        this.initLevel(); // Initialize level-specific elements
    }

    public void tick(Graphics g) {
        this.draw(g);
    }

    public void draw(Graphics g) {
        // Draw level elements

    }

    // protected void playerControls(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount) {
    //     if (pressedKeys.contains(java.awt.event.KeyEvent.VK_W)) {
    //         for (VisualObject[] row : visualObjects) {
    //             for (VisualObject obj : row) {
    //                 if (obj != null && obj.moveable) {
    //                     obj.move(0, -5); // Move up
    //                 }
    //             }
    //         }
    //     }
    // }

    protected void updateOffset(){

        if (!this.levelVisualObjects.get(0).isEmpty() && this.player instanceof Player && this.playerOnBorder()) {
            if (this.player.getX() < this.buffer) {
                this.offsetX += this.player.getSpeed();
                for (ArrayList<VisualObject> row : this.levelVisualObjects) {
                    for (VisualObject obj : row) {
                        if (obj != null && obj.moveable) {
                            obj.move((int) this.player.getSpeed(), 0); // Move right
                        }
                    }
                }
            } else if (this.player.getX() > 2000 - this.buffer) {
                this.offsetX -= this.player.getSpeed();
                for (ArrayList<VisualObject> row : this.levelVisualObjects) {
                    for (VisualObject obj : row) {
                        if (obj != null && obj.moveable) {
                            obj.move(-(int) this.player.getSpeed(), 0); // Move left
                        }
                    }
                }
            } else if (this.player.getY() < this.buffer) {
                this.offsetY += this.player.getSpeed();
                for (ArrayList<VisualObject> row : this.levelVisualObjects) {
                    for (VisualObject obj : row) {
                        if (obj != null && obj.moveable) {
                            obj.move(0, (int) this.player.getSpeed()); // Move down
                        }
                    }
                }
            } else if (this.player.getY() > 1000 - this.buffer) {
                this.offsetY -= this.player.getSpeed();
                for (ArrayList<VisualObject> row : this.levelVisualObjects) {
                    for (VisualObject obj : row) {
                        if (obj != null && obj.moveable) {
                            obj.move(0, -(int) this.player.getSpeed()); // Move up
                        }
                    }
                }
            }
        }
    }
    protected boolean playerOnBorder(){
        return (this.player.getX() < this.buffer || 
                this.player.getX() > 2000 - this.buffer ||
                this.player.getY() < this.buffer || 
                this.player.getY() > 1000 - this.buffer);
    }
    //child classes override to initialize their specific visual objects
    protected void initLevel() {
        // Initialize level-specific elements
    }
}
