package Levels;

import Objects.*;
import Objects.Buttons.*;
import Objects.EnemyClasses.*;
import Objects.PlayerClasses.Player;
import Objects.Terrain.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

    // Queues for deferred add/remove
    protected List<VisualObject> addList = new LinkedList<>();
    protected List<VisualObject> removeList = new LinkedList<>();

    public BaseLevel(int bgX, int bgY, String path, ArrayList<VisualObject> players) {
        this.bgX = bgX;
        this.bgY = bgY;
        this.initArrays(players);
        try {
            this.backgroundImage = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println("Image could not be loaded.");
            e.printStackTrace();
        }
        this.initLevel(); // Initialize level-specific elements
    }

    public BaseLevel(Color color, ArrayList<VisualObject> players) {
        this.initArrays(players);
        this.backgroundColor = color;
    }

    private void initArrays(ArrayList<VisualObject> players) {
        this.levelVisualObjects.add(players); // Row for Player objects
        this.levelVisualObjects.add(new ArrayList<>()); // Row for Wall objects
        this.levelVisualObjects.add(new ArrayList<>()); // Row for Enemy objects
        this.levelVisualObjects.add(new ArrayList<>()); // Row for Button objects
        this.player = (Player) players.get(0); // Set the main player
    }

    // Use this to queue an object for addition
    public void queueAddVisualObject(VisualObject obj) {
        if (obj != null) {
            addList.add(obj);
        }
    }

    // Use this to queue an object for removal
    public void queueRemoveVisualObject(VisualObject obj) {
        if (obj != null) {
            removeList.add(obj);
        }
    }

    // Call this after iterating over levelVisualObjects to apply changes
    public void processQueuedVisualObjectChanges() {
        // Remove objects
        for (VisualObject obj : removeList) {
            for (ArrayList<VisualObject> row : levelVisualObjects) {
                row.remove(obj);
            }
        }
        removeList.clear();

        // Add objects
        for (VisualObject obj : addList) {
            addVisualObject(obj);
        }
        addList.clear();
    }

    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount) {
        // Draw level elements
        this.drawBG(g);
        this.updateOffset();
        this.keyHandler(pressedKeys, clickXDown, clickYDown, tickCount);
        for (ArrayList<VisualObject> row : this.levelVisualObjects) {
            for (VisualObject obj : row) {
                if (obj != null) {
                    obj.tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, tickCount);
                }
            }
        }
        g.setColor(Color.WHITE);
        this.displayUI(g); // Display UI elements
        // After iteration, process queued changes
        this.processQueuedVisualObjectChanges();
        
        // System.out.println("BaseLevel tick: " + tickCount);
    }

    private void displayUI(Graphics g) {
        // Display UI elements like health, score, etc.
        g.setColor(Color.WHITE);
        int defaultX = 1750;
        int defaultY = 900;
        if (this.player.getX() > 1800 && this.player.getY() > 900) {
            defaultX = 10;
            defaultY = 20;
        }
        g.drawString("Health: " + this.player.getHealth() + "/" + this.player.getMaxHealth(), defaultX, defaultY);
        g.setColor(Color.RED);
        g.fillRect(defaultX, defaultY + 10, 100 * player.getHealth() / player.getMaxHealth(), 10);
        g.setColor(Color.WHITE);
        g.drawRect(defaultX, defaultY + 10, 100, 10);
        g.drawString("Rolls: " + this.player.rolls + "/" + this.player.maxRolls, defaultX, defaultY + 40);

        // Add more UI elements as needed
    }

    public void drawBG(Graphics g) {
        if (this.backgroundImage != null) {
            g.drawImage(this.backgroundImage, this.bgX, this.bgY, null);
        } else {
            g.setColor(this.backgroundColor != null ? this.backgroundColor : java.awt.Color.GRAY);
            g.fillRect(this.bgX, this.bgY, 1900, 1000);
        }
    }

    public void addVisualObject(VisualObject obj) {
        if (obj != null) {
            if (obj instanceof Button) {
                this.levelVisualObjects.get(3).add(obj); // Add to Button row
            } else if (obj instanceof Border) {
                this.levelVisualObjects.get(1).add(obj); // Add to Wall row
            } else if (obj instanceof Enemy) {
                this.levelVisualObjects.get(2).add(obj); // Add to Enemy row
            } else {
                this.levelVisualObjects.get(0).add(obj); // Add to Player row
            }
        }
    }

    public void keyHandler(Set<Integer> pressedKeys, int clickXDown, int clickYDown, int tickCount) {
        
            // Handle mouse click events

            int nextX = player.getX();
            int nextY = player.getY();
            int speed = (int) player.getSpeed();

            int moveX = 0;
            int moveY = 0;

            if (pressedKeys.contains(82) && player.rolls > 0) { // R
                if (pressedKeys.contains(65)) { // A
                    moveX -= 50 * speed;
                }
                if (pressedKeys.contains(68)) { // D
                    moveX += 50 * speed;
                }
                if (pressedKeys.contains(87)) { // W
                    moveY -= 50 * speed;
                }
                if (pressedKeys.contains(83)) { // S
                    moveY += 50 * speed;
                }
                player.rollCooldown(tickCount, 180); // Start roll cooldown
                player.resetAnimation();
            }
            if (!this.player.isInAnimation()) {
                if (pressedKeys.contains(65)) { // A
                    moveX -= speed;
                }
                if (pressedKeys.contains(68)) { // D
                    moveX += speed;
                }
                if (pressedKeys.contains(87)) { // W
                    moveY -= speed;
                }
                if (pressedKeys.contains(83)) { // S
                    moveY += speed;
                }
            }

            // Try to move as close as possible to the border
            int finalX = nextX;
            int finalY = nextY;

            // Move in X direction
            if (moveX != 0) {
                int stepX = moveX > 0 ? 1 : -1;
                for (int i = 0; i < Math.abs(moveX); i++) {
                    int testX = finalX + stepX;
                    boolean collides = false;
                    for (VisualObject obj : this.levelVisualObjects.get(1)) {
                        if (obj instanceof Border) {
                            if (testX < obj.getX() + obj.getWidth()
                                    && testX + player.getWidth() > obj.getX()
                                    && finalY < obj.getY() + obj.getHeight()
                                    && finalY + player.getHeight() > obj.getY()) {
                                collides = true;
                                break;
                            }
                        }
                    }
                    if (collides) {
                        break;
                    }
                    finalX = testX;
                }
            }

            // Move in Y direction
            if (moveY != 0) {
                int stepY = moveY > 0 ? 1 : -1;
                for (int i = 0; i < Math.abs(moveY); i++) {
                    int testY = finalY + stepY;
                    boolean collides = false;
                    for (VisualObject obj : this.levelVisualObjects.get(1)) {
                        if (obj instanceof Border) {
                            if (finalX < obj.getX() + obj.getWidth()
                                    && finalX + player.getWidth() > obj.getX()
                                    && testY < obj.getY() + obj.getHeight()
                                    && testY + player.getHeight() > obj.getY()) {
                                collides = true;
                                break;
                            }
                        }
                    }
                    if (collides) {
                        break;
                    }
                    finalY = testY;
                }
            }

            player.setX(finalX);
            player.setY(finalY);
            // else: do not move player
        
        
    }

    public void mouseHandler(int clickXDown, int clickYDown) {
        // Handle mouse click events
        if (this.player != null) {
            this.player.attack(clickXDown, clickYDown, 0, 0); // Pass dummy values for clickXUp and clickYUp
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
            } else if (this.player.getX() > 1900 - this.buffer) {
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
                || this.player.getX() > 1900 - this.buffer
                || this.player.getY() < this.buffer
                || this.player.getY() > 1000 - this.buffer);
    }

    //child classes override to initialize their specific visual objects
    protected void initLevel() {
        // Initialize level-specific elements
    }
    protected void addBorder(Border b) {
    	this.levelVisualObjects.get(1).add(b);
    }
    protected void addPlayer(Enemy e) {
    	this.levelVisualObjects.get(2).add(e);
    }
    protected void addButton(Button b) {
    	this.levelVisualObjects.get(3).add(b);
    }
}
