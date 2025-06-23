package Objects;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.imageio.ImageIO;

public class VisualObject {

    public int x, y, width, height;
    protected String imgPath;
    protected Image image;
    protected Boolean visible = true;
    protected Boolean held = false;

    public VisualObject(int x, int y, int width, int height, String imgPath) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        this.setImgPath(imgPath);
    }

    public VisualObject(int x, int y, String imgPath) {
        this.x = x;
        this.y = y;
        this.imgPath = imgPath;
        this.setImgPath(imgPath);
        this.width = this.image.getWidth(null);
        this.height = this.image.getHeight(null);
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
        if (imgPath != null){
        try {
            // Load the new image
            this.image = ImageIO.read(new File(this.imgPath));
        } catch (IOException e) {
            System.out.println("Image could not be loaded.");
            e.printStackTrace();
        }
    }
    }

    public void setVisible() {
        this.visible = true;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public void draw(Graphics g) {
        if (this.visible && this.image != null) {
            // Draw (blit) the image at position (50, 50)
            g.drawImage(this.image, this.x, this.y, this.width, this.height, null);
        }
    }

    public boolean checkCollision(VisualObject other) {
        if (!this.visible || !other.visible) {
            return false;
        }
        return this.x < other.x + other.width
                && this.x + this.width > other.x
                && this.y < other.y + other.height
                && this.y + this.height > other.y;
    }

    public boolean checkCollision(int x, int y) {
        if (!this.visible) {
            return false;
        }
        if (this.x < x && x < this.x + this.width && this.y < y && y < this.y + this.height){
            this.held = true;
            return true;
        }
        this.held = false;
        return false;
    }
    // Meant to be overridden by child classes, but ALWAYS CALL DRAW() !!!!
    public void tick(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount){
        this.draw(g);
        this.baseTickFunc(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, tickCount);
        this.specTickFunc(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, tickCount);
    }
    
    
    //Child classes override
    public void baseTickFunc(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount){

    }

    //Child classes override
    public void specTickFunc(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount){

    }
}
