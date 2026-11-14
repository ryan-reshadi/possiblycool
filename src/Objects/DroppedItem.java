package Objects;

import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Item;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Set;
import java.util.function.Consumer;

public class DroppedItem extends VisualObject {

    private final Item item;
    private final Consumer<Item> onPickup;
    private boolean pickedUp = false;

    public DroppedItem(int x, int y, Item item, Consumer<Item> onPickup) {
        super(x, y, 24, 24, item != null ? item.getIconColor() : Color.YELLOW);
        this.item = item;
        this.onPickup = onPickup;
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
        draw(g);
    }

    @Override
    public void draw(Graphics g) {
        if (pickedUp || !visible) return;

        Color base = item != null ? item.getIconColor() : Color.YELLOW;
        g.setColor(base);
        g.fillRect(x, y, width, height);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, width, height);

        if (item != null && item.getName() != null && !item.getName().isEmpty()) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            String initial = item.getName().substring(0, 1);
            g.drawString(initial, x + 8, y + 17);
        }
    }
}
