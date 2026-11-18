package Objects.Terrain;

import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Item;
import Objects.PlayerEquipment.LootTable;
import Objects.VisualObject;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.ArrayList;

public class Chest extends VisualObject {

    private boolean opened = false;
    private final List<Item> fixedContents;
    private final LootTable lootTable;
    private final int lootCount;
    private final Consumer<List<Item>> onOpen;
    private BiConsumer<List<Item>, Point> onOpenAt;

    public Chest(int x, int y, List<Item> contents, Consumer<List<Item>> onOpen) {
        super(x, y, 48, 36, new Color(139, 69, 19));
        this.fixedContents = contents == null ? new ArrayList<>() : new ArrayList<>(contents);
        this.lootTable = null;
        this.lootCount = 0;
        this.onOpen = onOpen;
        this.moveable = true;
    }

    public Chest(int x, int y, LootTable lootTable, int lootCount, Consumer<List<Item>> onOpen) {
        super(x, y, 48, 36, new Color(139, 69, 19));
        this.fixedContents = null;
        this.lootTable = lootTable;
        this.lootCount = Math.max(0, lootCount);
        this.onOpen = onOpen;
        this.moveable = true;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOnOpenAt(BiConsumer<List<Item>, Point> callback) {
        this.onOpenAt = callback;
    }

    public boolean tryOpen(Player player, int targetX, int targetY) {
        if (opened || player == null || !isWithinInteractionRange(player)
                || targetX < x || targetX > x + width || targetY < y || targetY > y + height) {
            return false;
        }
        opened = true;
        List<Item> contents = lootTable == null
                ? new ArrayList<>(fixedContents) : lootTable.roll(player, lootCount);
        if (onOpen != null) onOpen.accept(contents);
        if (onOpenAt != null) onOpenAt.accept(contents, new Point(x, y));
        return true;
    }

    private boolean isWithinInteractionRange(Player player) {
        double playerX = player.getX() + player.getWidth() / 2.0;
        double playerY = player.getY() + player.getHeight() / 2.0;
        double chestX = x + width / 2.0;
        double chestY = y + height / 2.0;
        return Math.hypot(playerX - chestX, playerY - chestY) <= 100.0;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (opened) {
                g2.setColor(new Color(70, 40, 18));
                g2.fillRoundRect(x, y + 6, width, height - 6, 6, 6);
                g2.setColor(new Color(110, 70, 30));
                g2.fillRoundRect(x + 2, y + 8, width - 4, height - 10, 4, 4);
                g2.setColor(new Color(190, 170, 90));
                g2.fillRect(x + 4, y + 4, width - 8, 6); // lid open
                g2.setColor(new Color(60, 60, 70));
                g2.drawRoundRect(x, y + 6, width, height - 6, 6, 6);
                g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                g2.setColor(new Color(220, 220, 200));
                g2.drawString("OPEN", x + 10, y + 28);
            } else {
                g2.setColor(new Color(120, 70, 30));
                g2.fillRoundRect(x, y, width, height, 6, 6);
                g2.setColor(new Color(170, 110, 55));
                g2.fillRoundRect(x + 2, y + 2, width - 4, height - 6, 4, 4);
                g2.setColor(new Color(90, 60, 20));
                g2.fillRect(x, y + height / 2 - 2, width, 4); // band
                g2.setColor(new Color(220, 190, 90));
                g2.fillRoundRect(x + width / 2 - 6, y + height / 2 - 6, 12, 12, 3, 3); // lock
                g2.setColor(new Color(255, 230, 140));
                g2.fillOval(x + width / 2 - 2, y + height / 2 - 2, 4, 4);
                g2.setColor(new Color(50, 30, 15));
                g2.drawRoundRect(x, y, width, height, 6, 6);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.setColor(new Color(240, 230, 200));
                g2.drawString("[Click] Chest", x - 2, y - 5);
            }
        } finally {
            g2.dispose();
        }
    }
}
