package GameRun;

import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

public class App extends JPanel {

    private int clickXDown = -1;
    private int clickYDown = -1;
    private int clickXUp = -1;
    private int clickYUp = -1;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Game game;

    public App() {
        // Add mouse listener to detect clicks
        setFocusable(true);
        this.game = new Game();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                clickXDown = e.getX();
                clickYDown = e.getY();
                // System.out.println("Mouse clicked at (" + clickXDown + ", " + clickYDown + ")");
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                clickXUp = e.getX();
                clickYUp = e.getY();
                // System.out.println("Mouse released at (" + clickXUp + ", " + clickYUp + ")");
            }
        });

        // Key listener to record key presses
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                pressedKeys.add(e.getKeyCode());
                // repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());
                // repaint();
            }
        });

        Timer timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();  // request a redraw
            }
        });
        timer.start();
    }

    private void tick(Graphics g) {
        this.game.tick(g, this.pressedKeys, this.clickXDown, this.clickYDown, this.clickXUp, this.clickYUp);  // Call the game's tick method
        this.clickXDown = -1;
        this.clickYDown = -1;
        this.clickXUp = -1;
        this.clickYUp = -1;
        // You can add game logic here that needs to run every tick
        // For example, you could update positions of game objects, check for collisions, etc.
        // System.out.println("Tick: " + tickCount);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.tick(g);

        g.drawString("Keys currently pressed: ", 10, 20);
        int y = 40;
        for (int keyCode : pressedKeys) {
            String keyName = KeyEvent.getKeyText(keyCode);
            g.drawString("- " + keyName, 10, y);
            y += 20;
        }
        game.screenUpdate(g, this.pressedKeys, this.clickXDown, this.clickYDown, this.clickXUp, this.clickYUp);
        // g.drawString("Tick count: " + tickCount, 150, 20);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SuperCoolThingy");
        App panel = new App();

        frame.add(panel);
        frame.setSize(2000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
