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
        g2d.setColor(new Color(20, 20, 25, 190));
        g2d.fillRoundRect(startX - 6, startY - 6, totalWidth + 12, slotSize + 12, 10, 10);
        g2d.setColor(new Color(60, 60, 75));
        g2d.drawRoundRect(startX - 6, startY - 6, totalWidth + 12, slotSize + 12, 10, 10);

        for (int i = 0; i < 3; i++) {
            int slotX = startX + i * (slotSize + padding);
            int slotY = startY;

            Ability ability = (abilities != null && i < abilities.length) ? abilities[i] : null;

            // Slot background
            g2d.setColor(new Color(35, 38, 48));
            g2d.fillRoundRect(slotX, slotY, slotSize, slotSize, 6, 6);

            if (ability != null) {
                // Ability icon
                g2d.setColor(ability.getIconColor() != null ? ability.getIconColor() : new Color(140, 60, 190));
                g2d.fillRoundRect(slotX + 5, slotY + 5, slotSize - 10, slotSize - 10, 4, 4);

                // Cooldown shadow overlay
                double progress = ability.getCooldownProgress(currentTick);
                if (progress < 1.0) {
                    Composite oldComp = g2d.getComposite();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
                    g2d.setColor(Color.BLACK);

                    int arcAngle = (int) Math.round((1.0 - progress) * 360);
                    Arc2D.Double cdArc = new Arc2D.Double(slotX + 2, slotY + 2, slotSize - 4, slotSize - 4, 90, arcAngle, Arc2D.PIE);
                    g2d.fill(cdArc);
                    g2d.setComposite(oldComp);
                }

                g2d.setColor(Color.WHITE);
                g2d.drawRoundRect(slotX + 5, slotY + 5, slotSize - 10, slotSize - 10, 4, 4);
            }

            // Key text badge
            g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2d.setColor(new Color(255, 230, 100));
            g2d.drawString(keys[i], slotX + 4, slotY + 13);

            g2d.setColor(new Color(90, 95, 110));
            g2d.drawRoundRect(slotX, slotY, slotSize, slotSize, 6, 6);
        }
    }
}
