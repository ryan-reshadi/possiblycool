package ShapeCore;

import Objects.PlayerEquipment.Ultimate;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

public final class UltimateBarUI {
    public void draw(Graphics g, Ultimate ultimate, double charge, int screenWidth, int screenHeight) {
        Graphics2D g2d = (Graphics2D) g;
        int width = 300;
        int x = (screenWidth - width) / 2;
        int y = screenHeight - 200;

        boolean active = ultimate != null && ultimate.isRoamingActive();
        double progress;
        if (active) {
            progress = Math.max(0.0, Math.min(1.0, ultimate.getActiveProgress()));
        } else {
            progress = Math.max(0.0, Math.min(1.0, charge / 100.0));
        }

        Color base = ultimate == null ? Color.GRAY : ultimate.getIconColor();
        Color fill = active ? new Color(255, 210, 90) : base;
        Color labelColor = active ? new Color(255, 230, 140) : base;

        g2d.setColor(new Color(20, 20, 28, 205));
        g2d.fillRoundRect(x - 10, y - 25, width + 20, 56, 10, 10);
        g2d.setColor(new Color(70, 70, 88));
        g2d.drawRoundRect(x - 10, y - 25, width + 20, 56, 10, 10);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2d.setColor(ultimate == null ? Color.LIGHT_GRAY : labelColor);
        String name = ultimate == null ? "No Ultimate" : ultimate.getName() + (active ? " (Active)" : "");
        g2d.drawString(name, x, y - 7);

        String hint = "[R]";
        if (active && ultimate != null && "battle-cry".equals(ultimate.getId())) {
            hint = "[R] End";
        } else if (!active && progress >= 1.0) {
            hint = "[R] Ready";
        }
        g2d.setColor(progress >= 1.0 || active ? new Color(255, 225, 95) : new Color(190, 195, 210));
        g2d.drawString(hint, x + width - g2d.getFontMetrics().stringWidth(hint), y - 7);

        g2d.setColor(new Color(48, 43, 60));
        g2d.fillRoundRect(x, y, width, 14, 7, 7);
        g2d.setColor(fill);
        g2d.fillRoundRect(x, y, (int) Math.round(width * progress), 14, 7, 7);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(x, y, width, 14, 7, 7);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
        String pct = active ? String.format("%.0f%%", progress * 100.0) : String.format("%.0f%%", charge);
        g2d.drawString(pct, x + width - 34, y + 11);
    }
}
