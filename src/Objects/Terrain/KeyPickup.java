package Objects.Terrain;

import Objects.PlayerClasses.Player;
import Objects.VisualObject;

import java.awt.*;
import java.util.function.Consumer;

public class KeyPickup extends VisualObject {
    private final String keyId;
    private final Consumer<String> onPickup;
    private boolean picked = false;

    public KeyPickup(int x, int y, String keyId, Consumer<String> onPickup) {
        super(x, y, 20, 20, (java.awt.Color) null);
        this.keyId = keyId == null ? "" : keyId;
        this.onPickup = onPickup;
    }

    public void checkPickup(Player player) {
        if (picked || player == null || onPickup == null) return;
        if (player.getX() < this.x + this.width && player.getX() + player.getWidth() > this.x &&
            player.getY() < this.y + this.height && player.getY() + player.getHeight() > this.y) {
            picked = true;
            onPickup.accept(keyId);
            setVisible(false);
        }
    }

    @Override
    public void update(int tickCount) {}

    @Override
    public void render(Graphics2D g, int tickCount) {
        g.setColor(new Color(240, 220, 80));
        g.fillOval(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawOval(x, y, width, height);
    }
}
