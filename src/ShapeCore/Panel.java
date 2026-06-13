package ShapeCore;

import java.awt.Color;
import java.awt.Graphics;

public class Panel {
    
    private int x;
    private int y;
    private int width;
    private int height;
    private Color fillColor;
    private Color outlineColor;
    private int outlineThickness;
    
    public Panel(int x, int y, int width, int height, Color fillColor, Color outlineColor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fillColor = fillColor;
        this.outlineColor = outlineColor;
        this.outlineThickness = 2;
    }
    
    public Panel(int x, int y, int width, int height, Color fillColor, Color outlineColor, int outlineThickness) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fillColor = fillColor;
        this.outlineColor = outlineColor;
        this.outlineThickness = outlineThickness;
    }
    
    public void draw(Graphics g) {
        // Draw filled rectangle
        g.setColor(fillColor);
        g.fillRect(x, y, width, height);
        
        // Draw outline rectangle
        g.setColor(outlineColor);
        for (int i = 0; i < outlineThickness; i++) {
            g.drawRect(x + i, y + i, width - 1 - (2 * i), height - 1 - (2 * i));
        }
    }
    
    public void setFillColor(Color color) {
        this.fillColor = color;
    }
    
    public void setOutlineColor(Color color) {
        this.outlineColor = color;
    }
    
    public void setOutlineThickness(int thickness) {
        this.outlineThickness = thickness;
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public Color getFillColor() {
        return fillColor;
    }
    
    public Color getOutlineColor() {
        return outlineColor;
    }
    
    public int getOutlineThickness() {
        return outlineThickness;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}
