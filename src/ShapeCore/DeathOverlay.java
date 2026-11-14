package ShapeCore;

import GameRun.Game;
import GameRun.GameStates;
import GameRun.InputFrame;
import GameRun.Viewport;
import ShapeCore.Buttons.Button;

import java.awt.*;

public class DeathOverlay {

    private final Game game;
    private final Button respawnButton;
    private final Button quitButton;

    public DeathOverlay(Game game) {
        this.game = game;

        this.respawnButton = new Button(850, 480, 200, 50, "Respawn", new Color(46, 139, 87), game) {
            @Override
            public void buttonAction() {
                if (game.player != null) {
                    game.player.respawnAtCheckpoint(400, 300);
                    game.changeState(GameStates.LEVEL_1);
                }
            }
        };

        this.quitButton = new Button(850, 550, 200, 50, "Main Menu", new Color(178, 34, 34), game) {
            @Override
            public void buttonAction() {
                game.changeState(GameStates.INITIALIZED_1);
            }
        };
    }

    public void update(InputFrame input, int tickCount) {
        respawnButton.update(input, tickCount);
        quitButton.update(input, tickCount);
    }

    public void render(Graphics2D g) {
        int screenWidth = Viewport.WIDTH;
        int screenHeight = Viewport.HEIGHT;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dark red vignette backdrop
        g.setColor(new Color(40, 0, 0, 210));
        g.fillRect(0, 0, screenWidth, screenHeight);

        // "YOU DIED" text
        g.setFont(new Font("Serif", Font.BOLD, 64));
        g.setColor(new Color(220, 20, 20));
        String title = "YOU DIED";
        FontMetrics fm = g.getFontMetrics();
        int tx = (screenWidth - fm.stringWidth(title)) / 2;
        g.drawString(title, tx, 380);

        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.setColor(new Color(200, 200, 210));
        String sub = "The dungeon claims another soul...";
        fm = g.getFontMetrics();
        int sx = (screenWidth - fm.stringWidth(sub)) / 2;
        g.drawString(sub, sx, 430);

        respawnButton.render(g, 0);
        quitButton.render(g, 0);
    }
}
