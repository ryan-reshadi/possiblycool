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
    private int mouseX = 0;
    private int mouseY = 0;
    private boolean isRightClick = false;
    private int scrollDelta = 0;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Game game;

    public App() {
        setFocusable(true);
        this.game = new Game();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                clickXDown = e.getX();
                clickYDown = e.getY();
                mouseX = e.getX();
                mouseY = e.getY();
                isRightClick = SwingUtilities.isRightMouseButton(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                clickXUp = e.getX();
                clickYUp = e.getY();
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                pressedKeys.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());
            }
        });

        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                scrollDelta = e.getWheelRotation();
            }
        });

        Timer timer = new Timer(17, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Update mouse position and right click on game
        this.game.setMousePosition(this.mouseX, this.mouseY);
        this.game.setRightClick(this.isRightClick);

        // Single screen update call per frame
        this.game.screenUpdate(g, this.pressedKeys, this.clickXDown, this.clickYDown, this.clickXUp, this.clickYUp, this.scrollDelta);

        // Reset click flags after the frame has consumed them
        this.clickXDown = -1;
        this.clickYDown = -1;
        this.clickXUp = -1;
        this.clickYUp = -1;
        this.isRightClick = false;
        this.scrollDelta = 0;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("possiblycool — Dungeon Crawler");
        App panel = new App();

        frame.add(panel);
        frame.setSize(1900, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
