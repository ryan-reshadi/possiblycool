package Objects.Terrain;

import Objects.PlayerClasses.Player;
import Objects.VisualObject;

import java.awt.*;
import java.util.function.Consumer;

public class PuzzleButton extends VisualObject {
    private final String puzzleId;
    private final Consumer<String> onSolved;
    private boolean solved = false;

    public PuzzleButton(int x, int y, String puzzleId, Consumer<String> onSolved) {
        super(x, y, 32, 32, (java.awt.Color) null);
        this.puzzleId = puzzleId == null ? "" : puzzleId;
        this.onSolved = onSolved;
    }

    public void tryActivate(Player player) {
        if (solved || player == null || onSolved == null) return;
        // simple proximity check
        double dx = (player.getX() + player.getWidth() / 2.0) - (x + width / 2.0);
        double dy = (player.getY() + player.getHeight() / 2.0) - (y + height / 2.0);
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 60) {
            solved = true;
            onSolved.accept(puzzleId);
        }
    }

    public boolean isSolved() {
        return solved;
    }

    @Override
    public void update(int tickCount) {
        // nothing
    }

    @Override
    public void render(Graphics2D g, int tickCount) {
        g.setColor(solved ? new Color(80, 220, 120) : new Color(220, 200, 80));
        g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
    }
}
