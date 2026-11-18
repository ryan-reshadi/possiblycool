package ShapeCore;

import Objects.PlayerEquipment.Ability;
import java.awt.*;
import java.awt.geom.Arc2D;

public class AbilityBarUI {

    private final int slotSize = 42;
    private final int padding = 8;
    private final String[] keys = {"Q", "E", "F"};

    public void draw(Graphics g, Ability[] abilities, int currentTick, int screenWidth, int screenHeight) {
        int totalWidth = 3 * (slotSize + padding) - padding;
        int startX = (screenWidth - totalWidth) / 2;
        int startY = screenHeight - slotSize - 80;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Backdrop
        g2d.setColor(new Color(18, 18, 28, 220));
        g2d.fillRoundRect(startX - 8, startY - 8, totalWidth + 16, slotSize + 16, 12, 12);
        g2d.setColor(new Color(90, 70, 140));
        g2d.drawRoundRect(startX - 8, startY - 8, totalWidth + 16, slotSize + 16, 12, 12);

        for (int i = 0; i < 3; i++) {
            int slotX = startX + i * (slotSize + padding);
            int slotY = startY;

            Ability ability = (abilities != null && i < abilities.length) ? abilities[i] : null;

            // Slot background
            g2d.setColor(new Color(30, 32, 45));
            g2d.fillRoundRect(slotX, slotY, slotSize, slotSize, 6, 6);

            if (ability != null) {
                double progress = ability.getCooldownProgress(currentTick);
                boolean ready = progress >= 1.0;
                if (ready) {
                    g2d.setColor(new Color(140, 200, 255, 30));
                    g2d.fillRoundRect(slotX - 1, slotY - 1, slotSize + 2, slotSize + 2, 8, 8);
                }
                // Ability icon
                Color ic = ability.getIconColor() != null ? ability.getIconColor() : new Color(140, 60, 190);
                g2d.setColor(ic);
                g2d.fillRoundRect(slotX + 5, slotY + 5, slotSize - 10, slotSize - 10, 4, 4);
                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.fillRoundRect(slotX + 7, slotY + 7, 6, slotSize - 14, 2, 2);

                // Cooldown shadow overlay
                if (!ready) {
                    Composite oldComp = g2d.getComposite();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2d.setColor(new Color(0, 0, 0));
                    int arcAngle = (int) Math.round((1.0 - progress) * 360);
                    Arc2D.Double cdArc = new Arc2D.Double(slotX + 2, slotY + 2, slotSize - 4, slotSize - 4, 90, arcAngle, Arc2D.PIE);
                    g2d.fill(cdArc);
                    g2d.setComposite(oldComp);
                }

                g2d.setColor(ready ? new Color(200, 230, 255) : new Color(200, 200, 210));
                g2d.drawRoundRect(slotX + 5, slotY + 5, slotSize - 10, slotSize - 10, 4, 4);
            }

            // Key text badge
            g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2d.setColor(new Color(255, 230, 100));
            g2d.drawString(keys[i], slotX + 4, slotY + 13);

            g2d.setColor(new Color(120, 100, 180));
            g2d.drawRoundRect(slotX, slotY, slotSize, slotSize, 6, 6);
        }
    }
}
