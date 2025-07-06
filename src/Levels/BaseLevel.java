package Levels;

import Objects.*;
import Objects.Buttons.*;
import Objects.PlayerClasses.Player;
import Objects.EnemyClasses.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import javax.imageio.ImageIO;


public class BaseLevel {

    protected int width;
    protected int height;
    protected ArrayList<ArrayList<VisualObject>> levelVisualObjects = new ArrayList<>();
    protected Image backgroundImage;
    protected int bgX = 0;
    protected int bgY = 0;
    // protected ArrayList<Player> players;
    protected Player player;
    protected int buffer = 50;
    protected int offsetX = 0;
    protected int offsetY = 0;
    protected Color backgroundColor = null;

    public BaseLevel(int bgX, int bgY, String path, ArrayList<Player> players) {
        this.bgX = bgX;
        this.bgY = bgY;
        this.levelVisualObjects = new ArrayList<>();
        this.levelVisualObjects.add(new ArrayList<>(players)); // Initialize with one empty row
        this.player = players.get(0); // Assuming the first player is the main player
        this.levelVisualObjects.add(new ArrayList<>()); // Row for Wall objects
        this.levelVisualObjects.add(new ArrayList<>()); // Row for Button objects
        this.levelVisualObjects.add(new ArrayList<>()); // Row for Enemy objects
        try {
            this.backgroundImage = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println("Image could not be loaded.");
            e.printStackTrace();
        }
        this.initLevel(); // Initialize level-specific elements
    }

    public BaseLevel(Color color, ArrayList<Player> players) {
        this.levelVisualObjects = new ArrayList<>();
        this.levelVisualObjects.add(new ArrayList<>(players)); // Initialize with one empty row
        this.player = players.get(0); // Assuming the first player is the main player
        this.backgroundColor = color;
    }


    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount) {
        // Draw level elements
        this.drawBG(g);
        this.updateOffset();
        for (ArrayList<VisualObject> row : this.levelVisualObjects) {
            for (VisualObject obj : row) {
                if (obj != null) {
                    obj.tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, tickCount);
                }
            }
        }
        // System.out.println("BaseLevel tick: " + tickCount);
    }

    public void drawBG(Graphics g) {
        if (this.backgroundImage != null) {
            g.drawImage(this.backgroundImage, this.bgX, this.bgY, null);
        } else {
            g.setColor(this.backgroundColor != null ? this.backgroundColor : java.awt.Color.GRAY);
            g.fillRect(this.bgX, this.bgY, 2000, 1000);
        }
    }
    public void addVisualObject(VisualObject obj) {
        if (obj != null) {
            if (obj instanceof Button){
                this.levelVisualObjects.get(3).add(obj); // Add to Button row
            } else if (obj instanceof Wall) {
                this.levelVisualObjects.get(1).add(obj); // Add to Wall row
            } else if (obj instanceof Enemy) {
                this.levelVisualObjects.get(2).add(obj); // Add to Enemy row
            } else {
                this.levelVisualObjects.get(0).add(obj); // Add to Player row
            }
        }
    }
    protected void updateOffset() {
        if (!this.levelVisualObjects.get(0).isEmpty() && this.player instanceof Player && this.playerOnBorder()) {
            if (this.player.getX() < this.buffer) {
                if (this.backgroundImage != null) {
                    this.bgX += this.player.getSpeed();
                }
                this.offsetX += this.player.getSpeed();
                for (ArrayList<VisualObject> row : this.levelVisualObjects) {
                    for (VisualObject obj : row) {
                        if (obj != null && obj.moveable) {
                            obj.move((int) this.player.getSpeed(), 0); // Move right
                        }
                    }
                }
            } else if (this.player.getX() > 2000 - this.buffer) {
                if (this.backgroundImage != null) {
                    this.bgX -= this.player.getSpeed();
                }
                this.offsetX -= this.player.getSpeed();
                for (ArrayList<VisualObject> row : this.levelVisualObjects) {
                    for (VisualObject obj : row) {
                        if (obj != null && obj.moveable) {
                            obj.move(-(int) this.player.getSpeed(), 0); // Move left
                        }
                    }
                }
            } else if (this.player.getY() < this.buffer) {
                if (this.backgroundImage != null) {
                    this.bgY += this.player.getSpeed();
                }
                this.offsetY += this.player.getSpeed();
                for (ArrayList<VisualObject> row : this.levelVisualObjects) {
                    for (VisualObject obj : row) {
                        if (obj != null && obj.moveable) {
                            obj.move(0, (int) this.player.getSpeed()); // Move down
                        }
                    }
                }
            } else if (this.player.getY() > 1000 - this.buffer) {
                if (this.backgroundImage != null) {
                    this.bgY -= this.player.getSpeed();
                }
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
    protected boolean playerOnBorder() {
        return (this.player.getX() < this.buffer
                || this.player.getX() > 2000 - this.buffer
                || this.player.getY() < this.buffer
                || this.player.getY() > 1000 - this.buffer);
    }

    //child classes override to initialize their specific visual objects
    protected void initLevel() {
        // Initialize level-specific elements
    }
}
