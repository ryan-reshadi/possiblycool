package ShapeCore;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class Label extends Panel {
    
    private String text;
    private Color textColor;
    private Font font;
    private TextAlignment alignment;
    
    public enum TextAlignment {
        LEFT, CENTER, RIGHT
    }
    
    public Label(int x, int y, int width, int height, Color fillColor, Color outlineColor, String text) {
        super(x, y, width, height, fillColor, outlineColor);
        this.text = text;
        this.textColor = Color.BLACK;
        this.font = new Font("Arial", Font.PLAIN, 12);
        this.alignment = TextAlignment.CENTER;
    }
    
    public Label(int x, int y, int width, int height, Color fillColor, Color outlineColor, int outlineThickness, String text) {
        super(x, y, width, height, fillColor, outlineColor, outlineThickness);
        this.text = text;
        this.textColor = Color.BLACK;
        this.font = new Font("Arial", Font.PLAIN, 12);
        this.alignment = TextAlignment.CENTER;
    }
    
    @Override
    public void draw(Graphics g) {
        // Draw the panel (fill and outline)
        super.draw(g);
        
        // Draw the text
        if (text != null && !text.isEmpty()) {
            g.setFont(font);
            g.setColor(textColor);
            
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            
            int x;
            switch (alignment) {
                case LEFT:
                    x = getX() + 5; // 5 pixel padding from left
                    break;
                case RIGHT:
                    x = getX() + getWidth() - textWidth - 5; // 5 pixel padding from right
                    break;
                case CENTER:
                default:
                    x = getX() + (getWidth() - textWidth) / 2;
                    break;
            }
            
            int y = getY() + (getHeight() - textHeight) / 2 + fm.getAscent();
            g.drawString(text, x, y);
        }
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public void setTextColor(Color color) {
        this.textColor = color;
    }
    
    public void setFont(Font font) {
        this.font = font;
    }
    
    public void setAlignment(TextAlignment alignment) {
        this.alignment = alignment;
    }
    
    public String getText() {
        return text;
    }
    
    public Color getTextColor() {
        return textColor;
    }
    
    public Font getFont() {
        return font;
    }
    
    public TextAlignment getAlignment() {
        return alignment;
    }
}
