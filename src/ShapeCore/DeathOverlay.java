package ShapeCore;

import GameRun.Game;
import GameRun.GameStates;
import GameRun.InputFrame;
import GameRun.Viewport;
import ShapeCore.Buttons.Button;

import java.awt.*;

public class DeathOverlay {

    private final Game game;
    private final Button endRunButton;
    private final Button quitButton;

    public DeathOverlay(Game game) {
        this.game = game;

        this.endRunButton = new Button(850, 480, 200, 50, "End Run", new Color(46, 139, 87), game) {
            @Override
            public void buttonAction() {
                game.endRunAfterDeath();
            }
        };

        this.quitButton = new Button(850, 550, 200, 50, "Main Menu", new Color(178, 34, 34), game) {
            @Override
            public void buttonAction() {
                game.endRunAfterDeath();
            }
        };
    }

    public void update(InputFrame input, int tickCount) {
        endRunButton.update(input, tickCount);
        quitButton.update(input, tickCount);
    }

    public void render(Graphics2D g) {
        int screenWidth = Viewport.WIDTH;
        int screenHeight = Viewport.HEIGHT;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dark red vignette backdrop
        g.setColor(new Color(30, 0, 0, 230));
        g.fillRect(0, 0, screenWidth, screenHeight);
        // radial red glow
        java.awt.RadialGradientPaint rg = new java.awt.RadialGradientPaint(
                new java.awt.geom.Point2D.Float(screenWidth / 2f, screenHeight / 2f - 40),
                400f,
                new float[]{0f, 1f},
                new Color[]{new Color(120, 20, 20, 80), new Color(0, 0, 0, 0)});
        g.setPaint(rg);
        g.fillRect(0, 0, screenWidth, screenHeight);

        // "YOU DIED" text
        g.setFont(new Font("Serif", Font.BOLD, 72));
        g.setColor(new Color(180, 10, 10));
        String title = "YOU DIED";
        FontMetrics fm = g.getFontMetrics();
        int tx = (screenWidth - fm.stringWidth(title)) / 2;
        g.drawString(title, tx + 2, 382);
        g.setColor(new Color(230, 40, 40));
        g.drawString(title, tx, 380);

        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.setColor(new Color(210, 200, 200));
        String sub = "The dungeon claims another soul...";
        fm = g.getFontMetrics();
        int sx = (screenWidth - fm.stringWidth(sub)) / 2;
        g.drawString(sub, sx, 440);

        endRunButton.render(g, 0);
        quitButton.render(g, 0);
    }
}
