package ShapeCore;

import javax.swing.*;
import java.awt.*;

public class ShapeTest extends JPanel {
    
    private Label labelCenter;
    private Label labelLeft;
    private Label labelRight;
    private Label labelCustomFont;
    private Label labelCustomColor;
    
    public ShapeTest() {
        // Create labels with different alignments
        labelCenter = new Label(50, 50, 250, 60, Color.BLUE, Color.BLACK, "Centered Text");
        labelCenter.setAlignment(Label.TextAlignment.CENTER);
        
        labelLeft = new Label(50, 130, 250, 60, Color.GREEN, Color.BLACK, "Left Aligned");
        labelLeft.setAlignment(Label.TextAlignment.LEFT);
        
        labelRight = new Label(50, 210, 250, 60, Color.YELLOW, Color.BLACK, "Right Aligned");
        labelRight.setAlignment(Label.TextAlignment.RIGHT);
        
        // Create label with custom font
        labelCustomFont = new Label(50, 290, 250, 60, Color.MAGENTA, Color.RED, 2, "Bold Font");
        labelCustomFont.setFont(new Font("Arial", Font.BOLD, 16));
        labelCustomFont.setAlignment(Label.TextAlignment.CENTER);
        
        // Create label with custom text color
        labelCustomColor = new Label(350, 50, 250, 60, Color.LIGHT_GRAY, Color.BLACK, 3, "White Text");
        labelCustomColor.setTextColor(Color.WHITE);
        labelCustomColor.setAlignment(Label.TextAlignment.CENTER);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw all labels
        labelCenter.draw(g);
        labelLeft.draw(g);
        labelRight.draw(g);
        labelCustomFont.draw(g);
        labelCustomColor.draw(g);
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Shape Test - Labels");
        ShapeTest testPanel = new ShapeTest();
        
        frame.add(testPanel);
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
