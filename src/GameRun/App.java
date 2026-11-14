package GameRun;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class App extends JPanel {
    private final InputState inputState = new InputState();
    private final FixedStepClock fixedStepClock = new FixedStepClock();
    private final Game game = new Game();
    private long lastUpdateNanos = System.nanoTime();

    public App() {
        setPreferredSize(new Dimension(Viewport.WIDTH, Viewport.HEIGHT));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                inputState.mousePressed(toLogicalX(e.getX()), toLogicalY(e.getY()),
                        SwingUtilities.isRightMouseButton(e));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                inputState.mouseReleased(toLogicalX(e.getX()), toLogicalY(e.getY()));
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                inputState.mouseMoved(toLogicalX(e.getX()), toLogicalY(e.getY()));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                inputState.mouseMoved(toLogicalX(e.getX()), toLogicalY(e.getY()));
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                inputState.keyPressed(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                inputState.keyReleased(e.getKeyCode());
            }
        });

        addMouseWheelListener((MouseWheelEvent e) -> inputState.mouseWheelMoved(e.getWheelRotation()));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                inputState.clear();
            }
        });

        Timer timer = new Timer(8, e -> advanceSimulation());
        timer.start();
    }

    private int toLogicalX(int x) {
        return Viewport.toLogicalX(x, getWidth(), getHeight());
    }

    private int toLogicalY(int y) {
        return Viewport.toLogicalY(y, getWidth(), getHeight());
    }

    private void advanceSimulation() {
        long now = System.nanoTime();
        long elapsed = Math.max(0, now - lastUpdateNanos);
        lastUpdateNanos = now;
        int steps = fixedStepClock.advance(elapsed);
        if (steps > 0) {
            InputFrame frame = inputState.snapshot();
            for (int step = 0; step < steps; step++) {
                game.update(frame);
                frame = frame.withoutEdges();
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            double scale = Viewport.scaleFor(getWidth(), getHeight());
            g2d.translate(Viewport.offsetXFor(getWidth(), getHeight()),
                    Viewport.offsetYFor(getWidth(), getHeight()));
            g2d.scale(scale, scale);
            game.render(g2d);
        } finally {
            g2d.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("possiblycool - Dungeon Crawler");
            App panel = new App();
            frame.setContentPane(panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(800, 600));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
