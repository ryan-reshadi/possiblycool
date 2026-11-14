package Objects.Terrain;

import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class Checkpoint extends VisualObject {

    private boolean activated = false;
    private final Runnable onActivate;

    public Checkpoint(int x, int y, Runnable onActivate) {
        super(x, y, 40, 40, Color.GREEN);
        this.onActivate = onActivate;
    }

    public boolean isActivated() {
        return activated;
    }

    public boolean checkActivation(Player player) {
        if (!activated && player != null && checkCollision(player)) {
            activated = true;
            if (onActivate != null) {
                onActivate.run();
            }
            return true;
        }
        return false;
    }

    @Override
    public void draw(Graphics g) {
        if (activated) {
            g.setColor(new Color(46, 139, 87)); // Darker green when active
        } else {
            g.setColor(new Color(50, 205, 50)); // Bright green
        }
        g.fillRect(x, y, width, height);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, width, height);

        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.drawString(activated ? "SAVED" : "SAVE", x + 4, y + 24);
    }
}
