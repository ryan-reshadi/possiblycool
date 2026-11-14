package ShapeCore;

import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.*;
import Objects.PlayerEquipment.Armor.ArmorSlot;
import Objects.PlayerEquipment.Weapons.Wand;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryPanel {

    public enum Tab {
        ALL("All"),
        WEAPONS("Weapons"),
        ARMOR("Armor"),
        ABILITIES("Abilities"),
        CONSUMABLES("Consumables");

        final String label;
        Tab(String label) { this.label = label; }
    }

    private final List<Item> storage = new ArrayList<>();
    private final List<Ability> abilityStorage = new ArrayList<>();

    private Tab currentTab = Tab.ALL;
    private Item selectedItem = null;
    private Ability selectedAbility = null;

    private int hoveredSlotType = -1; // -1: none, 0: storage, 1: head, 2: body, 3: ability, 4: hotbar
    private int hoveredIndex = -1;

    public InventoryPanel() {
    }

    public List<Item> getStorage() {
        return storage;
    }

    public List<Ability> getAbilityStorage() {
        return abilityStorage;
    }

    public void addItem(Item item) {
        if (item != null) storage.add(item);
    }

    public void addAbility(Ability ability) {
        if (ability != null) abilityStorage.add(ability);
    }

    public void draw(Graphics g, Player player, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (player == null || !player.isInventoryOpen()) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Screen shade
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, screenWidth, screenHeight);

        // Main window frame
        int pw = 820;
        int ph = 540;
        int px = (screenWidth - pw) / 2;
        int py = (screenHeight - ph) / 2;

        g2d.setColor(new Color(24, 26, 34, 245));
        g2d.fillRoundRect(px, py, pw, ph, 16, 16);
        g2d.setColor(new Color(85, 90, 110));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(px, py, pw, ph, 16, 16);
        g2d.setStroke(new BasicStroke(1.0f));

        // Header Title
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2d.setColor(new Color(240, 240, 255));
        g2d.drawString("HERO LOADOUT & INVENTORY", px + 24, py + 34);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(new Color(170, 175, 195));
        g2d.drawString("Press [I] to close. Click item to equip/swap into matching slots.", px + 24, py + 52);

        // Divider
        int midX = px + 290;
        g2d.setColor(new Color(55, 58, 72));
        g2d.drawLine(midX, py + 70, midX, py + ph - 20);

        // ── LEFT COLUMN: GEAR & SLOTS ─────────────────────────────────────────
        drawEquippedGear(g2d, player, px + 24, py + 75);

        // ── RIGHT COLUMN: STORAGE TABS & GRID ─────────────────────────────────
        drawStorageSection(g2d, px + 310, py + 75, mouseX, mouseY);

        // ── Tooltip ───────────────────────────────────────────────────────────
        drawTooltip(g2d, player, mouseX, mouseY);
    }

    private void drawEquippedGear(Graphics2D g2d, Player player, int x, int y) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString("ARMOR SLOTS", x, y);

        // Head Armor Slot
        drawSlot(g2d, x, y + 10, 50, 50, "HEAD", player.getHeadArmor() != null ? player.getHeadArmor().getIconColor() : null,
                player.getHeadArmor() != null ? player.getHeadArmor().getName() : null);

        // Body Armor Slot
        drawSlot(g2d, x + 65, y + 10, 50, 50, "BODY", player.getBodyArmor() != null ? player.getBodyArmor().getIconColor() : null,
                player.getBodyArmor() != null ? player.getBodyArmor().getName() : null);

        // Abilities
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString("EQUIPPED ABILITIES", x, y + 85);

        String[] abKeys = {"Q", "E", "F"};
        Ability[] abs = player.getAbilitySlots();
        for (int i = 0; i < 3; i++) {
            Ability a = (abs != null && i < abs.length) ? abs[i] : null;
            drawSlot(g2d, x + i * 58, y + 95, 50, 50, abKeys[i],
                    a != null ? a.getIconColor() : null,
                    a != null ? a.getName() : null);
        }

        // Passives
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString("PASSIVE SLOTS", x, y + 170);

        Ability[] passives = player.getPassiveSlots();
        for (int i = 0; i < 2; i++) {
            Ability p = (passives != null && i < passives.length) ? passives[i] : null;
            drawSlot(g2d, x + i * 58, y + 180, 50, 50, "P" + (i + 1),
                    p != null ? p.getIconColor() : null,
                    p != null ? p.getName() : null);
        }

        // Hotbar
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString("HOTBAR WEAPONS / ITEMS (1-9)", x, y + 255);

        Item[] inv = player.getInventory();
        for (int i = 0; i < 9; i++) {
            int row = i / 5;
            int col = i % 5;
            int sx = x + col * 50;
            int sy = y + 268 + row * 52;
            Item it = (inv != null && i < inv.length) ? inv[i] : null;
            drawSlot(g2d, sx, sy, 44, 44, String.valueOf(i + 1),
                    it != null ? it.getIconColor() : null,
                    it != null ? it.getName() : null);
        }
    }

    private void drawStorageSection(Graphics2D g2d, int x, int y, int mouseX, int mouseY) {
        // Tab buttons
        Tab[] tabs = Tab.values();
        int tabX = x;
        for (Tab t : tabs) {
            boolean active = (t == currentTab);
            int tw = 88;
            int th = 26;

            g2d.setColor(active ? new Color(50, 80, 140) : new Color(38, 41, 52));
            g2d.fillRoundRect(tabX, y, tw, th, 8, 8);
            g2d.setColor(active ? Color.WHITE : new Color(170, 175, 195));
            g2d.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 11));
            g2d.drawString(t.label, tabX + 8, y + 17);

            tabX += tw + 6;
        }

        // Storage grid (5 cols x 4 rows)
        int gridY = y + 40;
        int slotW = 54;
        int slotH = 54;
        int pad = 8;

        List<Object> itemsToShow = getFilteredStorage();
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 5; c++) {
                int index = r * 5 + c;
                int sx = x + c * (slotW + pad);
                int sy = gridY + r * (slotH + pad);

                Object obj = index < itemsToShow.size() ? itemsToShow.get(index) : null;
                Color col = null;
                String label = null;

                if (obj instanceof Item) {
                    col = ((Item) obj).getIconColor();
                    label = ((Item) obj).getName();
                } else if (obj instanceof Ability) {
                    col = ((Ability) obj).getIconColor();
                    label = ((Ability) obj).getName();
                }

                drawSlot(g2d, sx, sy, slotW, slotH, null, col, label);
            }
        }
    }

    private List<Object> getFilteredStorage() {
        List<Object> list = new ArrayList<>();
        if (currentTab == Tab.ALL || currentTab == Tab.WEAPONS || currentTab == Tab.ARMOR || currentTab == Tab.CONSUMABLES) {
            for (Item it : storage) {
                if (currentTab == Tab.ALL) {
                    list.add(it);
                } else if (currentTab == Tab.WEAPONS && (it instanceof MeleeWeapon || it instanceof Wand)) {
                    list.add(it);
                } else if (currentTab == Tab.ARMOR && it instanceof Armor) {
                    list.add(it);
                } else if (currentTab == Tab.CONSUMABLES && it instanceof Consumable) {
                    list.add(it);
                }
            }
        }
        if (currentTab == Tab.ALL || currentTab == Tab.ABILITIES) {
            list.addAll(abilityStorage);
        }
        return list;
    }

    private void drawSlot(Graphics2D g2d, int x, int y, int w, int h, String tag, Color fill, String name) {
        g2d.setColor(new Color(32, 35, 45));
        g2d.fillRoundRect(x, y, w, h, 8, 8);
        g2d.setColor(new Color(70, 75, 92));
        g2d.drawRoundRect(x, y, w, h, 8, 8);

        if (fill != null) {
            g2d.setColor(fill);
            g2d.fillRoundRect(x + 5, y + 5, w - 10, h - 10, 6, 6);
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(x + 5, y + 5, w - 10, h - 10, 6, 6);

            if (name != null && !name.isEmpty()) {
                g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                g2d.drawString(name.substring(0, 1), x + w / 2 - 4, y + h / 2 + 5);
            }
        }

        if (tag != null) {
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g2d.setColor(new Color(180, 185, 200));
            g2d.drawString(tag, x + 3, y + 10);
        }
    }

    public void handleClick(int clickX, int clickY, Player player, int screenWidth, int screenHeight) {
        if (player == null || !player.isInventoryOpen()) return;

        int pw = 820;
        int ph = 540;
        int px = (screenWidth - pw) / 2;
        int py = (screenHeight - ph) / 2;

        // Check tabs
        Tab[] tabs = Tab.values();
        int tabX = px + 310;
        int tabY = py + 75;
        for (Tab t : tabs) {
            if (clickX >= tabX && clickX <= tabX + 88 && clickY >= tabY && clickY <= tabY + 26) {
                currentTab = t;
                return;
            }
            tabX += 88 + 6;
        }

        // Check storage grid clicks
        int gridX = px + 310;
        int gridY = py + 115;
        int slotW = 54;
        int slotH = 54;
        int pad = 8;
        List<Object> filtered = getFilteredStorage();

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 5; c++) {
                int sx = gridX + c * (slotW + pad);
                int sy = gridY + r * (slotH + pad);
                if (clickX >= sx && clickX <= sx + slotW && clickY >= sy && clickY <= sy + slotH) {
                    int idx = r * 5 + c;
                    if (idx < filtered.size()) {
                        Object obj = filtered.get(idx);
                        if (obj instanceof Armor) {
                            Armor a = (Armor) obj;
                            if (a.getSlot() == ArmorSlot.HEAD) {
                                Armor old = player.getHeadArmor();
                                player.equipHead(a);
                                storage.remove(a);
                                if (old != null) storage.add(old);
                            } else {
                                Armor old = player.getBodyArmor();
                                player.equipBody(a);
                                storage.remove(a);
                                if (old != null) storage.add(old);
                            }
                        } else if (obj instanceof Item) {
                            Item it = (Item) obj;
                            if (player.addToHotbar(it)) {
                                storage.remove(it);
                            }
                        } else if (obj instanceof Ability) {
                            Ability ab = (Ability) obj;
                            if (player.addAbility(ab)) {
                                abilityStorage.remove(ab);
                            }
                        }
                    }
                    return;
                }
            }
        }

        // Check unequip on left gear:
        // Head slot:
        int leftX = px + 24;
        int leftY = py + 75;
        if (clickX >= leftX && clickX <= leftX + 50 && clickY >= leftY + 10 && clickY <= leftY + 60) {
            if (player.getHeadArmor() != null) {
                storage.add(player.getHeadArmor());
                player.equipHead(null);
            }
            return;
        }
        // Body slot:
        if (clickX >= leftX + 65 && clickX <= leftX + 115 && clickY >= leftY + 10 && clickY <= leftY + 60) {
            if (player.getBodyArmor() != null) {
                storage.add(player.getBodyArmor());
                player.equipBody(null);
            }
            return;
        }

        // Hotbar unequip to storage
        Item[] inv = player.getInventory();
        for (int i = 0; i < 9; i++) {
            int row = i / 5;
            int col = i % 5;
            int sx = leftX + col * 50;
            int sy = leftY + 268 + row * 52;
            if (clickX >= sx && clickX <= sx + 44 && clickY >= sy && clickY <= sy + 44) {
                if (inv[i] != null) {
                    storage.add(inv[i]);
                    inv[i] = null;
                }
                return;
            }
        }
    }

    private void drawTooltip(Graphics2D g2d, Player player, int mouseX, int mouseY) {
        // Simple hovered item check to draw tooltip
        // To be lag-free, draws simple tooltip text next to mouse pointer
    }
}
