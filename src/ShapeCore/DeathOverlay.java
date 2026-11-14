package ShapeCore;

import GameRun.Game;
import GameRun.GameStates;
import Objects.PlayerClasses.Player;
import ShapeCore.Buttons.Button;

import java.awt.*;
import java.util.Set;

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

    public void draw(Graphics g, Set<Integer> pressedKeys, int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount, int screenWidth, int screenHeight) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dark red vignette backdrop
        g2d.setColor(new Color(40, 0, 0, 210));
        g2d.fillRect(0, 0, screenWidth, screenHeight);

        // "YOU DIED" text
        g2d.setFont(new Font("Serif", Font.BOLD, 64));
        g2d.setColor(new Color(220, 20, 20));
        String title = "YOU DIED";
        FontMetrics fm = g2d.getFontMetrics();
        int tx = (screenWidth - fm.stringWidth(title)) / 2;
        g2d.drawString(title, tx, 380);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g2d.setColor(new Color(200, 200, 210));
        String sub = "The dungeon claims another soul...";
        fm = g2d.getFontMetrics();
        int sx = (screenWidth - fm.stringWidth(sub)) / 2;
        g2d.drawString(sub, sx, 430);

        // Tick & draw buttons
        respawnButton.tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, tickCount);
        quitButton.tick(g, pressedKeys, clickXDown, clickYDown, clickXUp, clickYUp, tickCount);
    }
}
