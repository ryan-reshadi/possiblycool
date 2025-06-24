package Levels;

import Objects.*;
import Objects.PlayerClasses.Player;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.imageio.ImageIO;

public class BaseLevel {

    protected int width;
    protected int height;
    protected VisualObject[][] visualObjects;
    protected Image backgroundImage;
    protected int bgX, bgY;
    protected Player player;

    public BaseLevel(int bgX, int bgY, String path, Player player) {
        this.bgX = bgX;
        this.bgY = bgY;
        this.player = player;
        try {
            this.backgroundImage = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println("Image could not be loaded.");
            e.printStackTrace();
        }
    }

    public void tick() {

    }

    public void draw(Graphics g) {
        // Draw level elements

    }

    protected void playerControls(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount) {
        if (pressedKeys.contains(java.awt.event.KeyEvent.VK_W)) {
            for (VisualObject[] row : visualObjects) {
                for (VisualObject obj : row) {
                    if (obj != null && obj.moveable) {
                        obj.move(0, -5); // Move up
                    }
                }
            }
        }
    }
}
