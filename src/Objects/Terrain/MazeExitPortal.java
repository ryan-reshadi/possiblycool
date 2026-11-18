package Objects.Terrain;

import Objects.PlayerClasses.Player;
import Objects.VisualObject;

import java.awt.*;
import java.util.function.Consumer;

public class MazeExitPortal extends VisualObject {
    private final String puzzleId;
    private final Consumer<String> onSolved;
    private boolean activated = false;

    public MazeExitPortal(int x, int y, int w, int h, String puzzleId, Consumer<String> onSolved) {
        super(x, y, w, h, (java.awt.Color) null);
        this.puzzleId = puzzleId;
        this.onSolved = onSolved;
    }

    public void check(Player player) {
        if (activated || player == null || onSolved == null) return;
        if (player.getX() < this.x + this.width && player.getX() + player.getWidth() > this.x &&
            player.getY() < this.y + this.height && player.getY() + player.getHeight() > this.y) {
            activated = true;
            onSolved.accept(puzzleId);
        }
    }

    @Override
    public void update(int tickCount) {}

    @Override
    public void render(Graphics2D g, int tickCount) {
        g.setColor(new Color(100, 180, 255, 180));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(200, 230, 255));
        g.drawRect(x, y, width, height);
    }
}
