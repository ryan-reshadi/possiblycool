package Objects;

import GameRun.AssetLoader;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Set;

public class VisualObject {

    public int x, y, width, height;
    protected String imgPath;
    protected Image image = null;
    protected Boolean visible = true;
    protected Boolean held = false;
    public Boolean moveable = true;
    protected Color color = null;

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
        if (this.image == null) {
            throw new IllegalArgumentException("An image resource is required for this constructor.");
        }
        this.width = this.image.getWidth(null);
        this.height = this.image.getHeight(null);
    }

    public VisualObject(int x, int y, int width, int height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
        this.image = imgPath == null ? null : AssetLoader.loadImage(imgPath);
    }

    public void scaleImage(int newWidth, int newHeight) {
        if (this.image != null) {
            this.image = this.image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            this.width = newWidth;
            this.height = newHeight;
        } else {
            System.out.println("Image not set, cannot scale.");
        }
    }

    public int secondsToTicks(double seconds) {
        return (int) (seconds * 60); // Assuming 60 ticks per second
    }
    public void setVisible() {
        this.visible = true;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return Boolean.TRUE.equals(visible);
    }

    public void draw(Graphics g) {
        if (this.visible) {
            if (this.image != null) {
                // Draw (blit) the image at position (50, 50)
                g.drawImage(this.image, this.x, this.y, this.width, this.height, null);
            }
            else {
                g.setColor(this.color != null ? this.color : Color.yellow);
                g.fillRect(this.x, this.y, this.width, this.height);
            }
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

    public boolean checkCollision(VisualObject other, int offsetX, int offsetY) {
        if (!this.visible || !other.visible) {
            return false;
        }
        return this.x + offsetX < other.x + other.width
                && this.x + this.width + offsetX > other.x
                && this.y + offsetY < other.y + other.height
                && this.y + this.height + offsetY > other.y;
    }

    public boolean checkCollision(int x, int y) {
        if (!this.visible) {
            return false;
        }
        if (this.x < x && x < this.x + this.width && this.y < y && y < this.y + this.height) {
            this.held = true;
            return true;
        }
        this.held = false;
        return false;
    }

    public void update(int tickCount) {
    }

    public void render(Graphics2D g, int tickCount) {
        this.draw(g);
    }
    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }
    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }
    public int getWidth() {
        return this.width;
    }
    
    public int getHeight() {
        return this.height;
    }
    public void testWorking() {
    	System.out.println("This object is working!");
    }
}
