package ShapeCore;

import Objects.PlayerEquipment.Item;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class HotbarUI {

    private final int slotSize = 48;
    private final int padding = 6;
    private final int totalWidth = 9 * (slotSize + padding) - padding;

    private static final Map<String, Image> spriteCache = new HashMap<>();

    private Image getSprite(String path) {
        if (path == null) return null;
        if (spriteCache.containsKey(path)) return spriteCache.get(path);

        try {
            File f = new File(path);
            if (f.exists()) {
                Image img = ImageIO.read(f);
                spriteCache.put(path, img);
                return img;
            }
        } catch (IOException e) {
            // ignore
        }
        return null;
    }

    public void draw(Graphics g, Item[] hotbar, int activeIndex, int screenWidth, int screenHeight) {
        if (hotbar == null) return;

        int startX = (screenWidth - totalWidth) / 2;
        int startY = screenHeight - slotSize - 20;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Backdrop bar
        g2d.setColor(new Color(20, 20, 25, 210));
        g2d.fillRoundRect(startX - 8, startY - 8, totalWidth + 16, slotSize + 16, 12, 12);
        g2d.setColor(new Color(70, 70, 85));
        g2d.drawRoundRect(startX - 8, startY - 8, totalWidth + 16, slotSize + 16, 12, 12);

        for (int i = 0; i < 9 && i < hotbar.length; i++) {
            int slotX = startX + i * (slotSize + padding);
            int slotY = startY;

            // Slot background
            g2d.setColor(new Color(40, 42, 50));
            g2d.fillRoundRect(slotX, slotY, slotSize, slotSize, 8, 8);

            // Active slot gold highlight
            if (i == activeIndex) {
                g2d.setColor(new Color(255, 215, 0));
                g2d.setStroke(new BasicStroke(2.5f));
                g2d.drawRoundRect(slotX, slotY, slotSize, slotSize, 8, 8);
                g2d.setStroke(new BasicStroke(1.0f));
            } else {
                g2d.setColor(new Color(90, 95, 110));
                g2d.drawRoundRect(slotX, slotY, slotSize, slotSize, 8, 8);
            }

            // Draw item if present
            Item item = hotbar[i];
            if (item != null) {
                Image sprite = getSprite(item.getSpritePath());
                if (sprite != null) {
                    int sw = 32;
                    int sh = 32;
                    int sx = slotX + (slotSize - sw) / 2;
                    int sy = slotY + (slotSize - sh) / 2;
                    g2d.drawImage(sprite, sx, sy, sw, sh, null);
                } else {
                    Color iconColor = item.getIconColor();
                    g2d.setColor(iconColor != null ? iconColor : Color.LIGHT_GRAY);
                    g2d.fillRoundRect(slotX + 8, slotY + 8, slotSize - 16, slotSize - 16, 6, 6);
                    g2d.setColor(Color.WHITE);
                    g2d.drawRoundRect(slotX + 8, slotY + 8, slotSize - 16, slotSize - 16, 6, 6);

                    // First letter
                    if (item.getName() != null && !item.getName().isEmpty()) {
                        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                        g2d.drawString(item.getName().substring(0, 1), slotX + slotSize / 2 - 4, slotY + slotSize / 2 + 5);
                    }
                }
            }

            // Slot key indicator (1..9)
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2d.setColor(new Color(180, 185, 200));
            g2d.drawString(String.valueOf(i + 1), slotX + 4, slotY + 12);
        }
    }
}
