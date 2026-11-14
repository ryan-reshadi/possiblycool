package Objects.Terrain;

import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Item;
import Objects.PlayerEquipment.LootTable;
import Objects.VisualObject;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
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
        if (opened) {
            g.setColor(new Color(90, 50, 20));
            g.fillRect(x, y, width, height);
            g.setColor(Color.GRAY);
            g.drawRect(x, y, width, height);
            g.drawString("OPEN", x + 10, y + 22);
        } else {
            g.setColor(new Color(160, 82, 45));
            g.fillRect(x, y, width, height);
            g.setColor(Color.ORANGE);
            g.fillRect(x + width / 2 - 4, y + height / 2 - 4, 8, 8); // Gold lock
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, height);
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g.drawString("[Click] Chest", x - 2, y - 5);
        }
    }
}
