package Objects;

import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Item;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

public class DroppedItem extends VisualObject {

    private final Item item;
    private final Consumer<Item> onPickup;
    private boolean pickedUp = false;
    private Image sprite = null;

    private static final Map<String, Image> imageCache = new HashMap<>();

    public DroppedItem(int x, int y, Item item, Consumer<Item> onPickup) {
        super(x, y, 32, 32, item != null ? item.getIconColor() : Color.YELLOW);
        this.item = item;
        this.onPickup = onPickup;

        if (item != null && item.getSpritePath() != null) {
            loadSprite(item.getSpritePath());
        }
    }

    private void loadSprite(String path) {
        if (imageCache.containsKey(path)) {
            this.sprite = imageCache.get(path);
            return;
        }

        try {
            File f = new File(path);
            if (f.exists()) {
                Image img = ImageIO.read(f);
                imageCache.put(path, img);
                this.sprite = img;
            }
        } catch (IOException e) {
            System.err.println("Failed to load drop sprite: " + path);
        }
    }

    public Item getItem() {
        return item;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public boolean checkPickup(Player player) {
        if (pickedUp || !visible || player == null || player.isDead()) {
            return false;
        }

        if (checkCollision(player)) {
            if (item != null && !item.canBeUsedBy(player)) {
                return false;
            }
            pickedUp = true;
            this.visible = false;
            if (onPickup != null) {
                onPickup.accept(item);
            }
            return true;
        }
        return false;
    }

    @Override
    public void tick(Graphics g, Set<Integer> pressedKeys,
                     int clickXDown, int clickYDown, int clickXUp, int clickYUp, int tickCount) {
        if (pickedUp || !visible) return;
        draw(g, tickCount);
    }

    public void draw(Graphics g, int tickCount) {
        if (pickedUp || !visible) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Bobbing floating animation
        int bobY = (int) (Math.sin(tickCount * 0.12) * 3);

        // Ground shadow
        g2d.setColor(new Color(0, 0, 0, 70));
        g2d.fillOval(x + 4, y + height - 2, width - 8, 6);

        // Soft pulsing glow
        float glowAlpha = 0.25f + (float) (Math.sin(tickCount * 0.08) * 0.15);
        Composite oldComp = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.1f, glowAlpha)));
        Color glowColor = (item != null && item.getIconColor() != null) ? item.getIconColor() : Color.YELLOW;
        g2d.setColor(glowColor);
        g2d.fillOval(x - 3, y + bobY - 3, width + 6, height + 6);
        g2d.setComposite(oldComp);

        // Render sprite if available
        if (this.sprite != null) {
            g2d.drawImage(this.sprite, x, y + bobY, width, height, null);
        } else {
            // Procedural fallback
            Color base = item != null ? item.getIconColor() : Color.YELLOW;
            g2d.setColor(base);
            g2d.fillRoundRect(x + 2, y + bobY + 2, width - 4, height - 4, 8, 8);
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(x + 2, y + bobY + 2, width - 4, height - 4, 8, 8);

            if (item != null && item.getName() != null && !item.getName().isEmpty()) {
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                String initial = item.getName().substring(0, 1);
                g2d.drawString(initial, x + 11, y + bobY + 21);
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        draw(g, 0);
    }
}
