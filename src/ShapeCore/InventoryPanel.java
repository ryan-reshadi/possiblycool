package ShapeCore;

import GameRun.AssetLoader;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.*;
import Objects.PlayerEquipment.Armor.ArmorSlot;
import Objects.PlayerEquipment.Weapons.Hammer;
import Objects.PlayerEquipment.Weapons.Wand;

import java.awt.*;
import java.util.*;
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
    private Object draggedObject;
    private int dragStartX;
    private int dragStartY;

    public InventoryPanel() {
    }

    public List<Item> getStorage() {
        return storage;
    }

    public List<Ability> getAbilityStorage() {
        return abilityStorage;
    }

    public void addItem(Item item) {
        if (item == null) return;
        if (item instanceof Consumable) {
            Consumable incoming = (Consumable) item;
            if (incoming.isUsed()) return;
            for (Item stored : storage) {
                if (stored instanceof Consumable) {
                    ((Consumable) stored).mergeFrom(incoming);
                    if (incoming.isUsed()) return;
                }
            }
        }
        storage.add(item);
    }

    public void addAbility(Ability ability) {
        if (ability != null) abilityStorage.add(ability);
    }

    private Image getSprite(String path) {
        if (path == null) return null;
        return AssetLoader.loadImage(path);
    }

    public void draw(Graphics g, Player player, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (player == null || !player.isInventoryOpen()) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Screen shade
        g2d.setColor(new Color(0, 0, 0, 175));
        g2d.fillRect(0, 0, screenWidth, screenHeight);

        // Main window frame
        int pw = 840;
        int ph = 560;
        int px = (screenWidth - pw) / 2;
        int py = (screenHeight - ph) / 2;

        g2d.setColor(new Color(22, 24, 32, 250));
        g2d.fillRoundRect(px, py, pw, ph, 16, 16);
        g2d.setColor(new Color(85, 92, 115));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(px, py, pw, ph, 16, 16);
        g2d.setStroke(new BasicStroke(1.0f));

        // Header Title
        g2d.setFont(new Font("SansSerif", Font.BOLD, 19));
        g2d.setColor(new Color(245, 245, 255));
        g2d.drawString("HERO LOADOUT & INVENTORY", px + 24, py + 36);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(new Color(175, 180, 200));
        g2d.drawString("Press [I] to close. Left-Click to equip/move. Right-Click consumables to use.", px + 24, py + 54);

        // Vertical Divider
        int midX = px + 295;
        g2d.setColor(new Color(55, 60, 75));
        g2d.drawLine(midX, py + 72, midX, py + ph - 20);

        // ── LEFT COLUMN: GEAR & SLOTS ─────────────────────────────────────────
        drawEquippedGear(g2d, player, px + 24, py + 75);

        // ── RIGHT COLUMN: STORAGE TABS & GRID ─────────────────────────────────
        drawStorageSection(g2d, px + 315, py + 75, mouseX, mouseY);

        // ── TOOLTIP ───────────────────────────────────────────────────────────
        drawTooltip(g2d, player, mouseX, mouseY, px, py, screenWidth, screenHeight);
        drawDraggedItem(g2d, mouseX, mouseY);
    }

    private void drawDraggedItem(Graphics2D g2d, int mouseX, int mouseY) {
        if (draggedObject == null) return;
        String name;
        String spritePath;
        Color color;
        if (draggedObject instanceof Item) {
            Item item = (Item) draggedObject;
            name = item.getName(); spritePath = item.getSpritePath(); color = item.getIconColor();
        } else {
            Ability ability = (Ability) draggedObject;
            name = ability.getName(); spritePath = ability.getSpritePath(); color = ability.getIconColor();
        }
        int x = mouseX - 23, y = mouseY - 23;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.82f));
        drawSlot(g2d, x, y, 46, 46, null, color, name, spritePath);
        g2d.setComposite(AlphaComposite.SrcOver);
    }

    /** Start a drag on press and commit it on release; plain clicks keep their existing actions. */
    public void handlePointer(int downX, int downY, int upX, int upY, boolean rightClick,
                              Player player, int screenWidth, int screenHeight, int currentTick) {
        if (player == null || !player.isInventoryOpen()) return;
        if (downX != -1 && downY != -1) {
            if (rightClick) {
                handleClick(downX, downY, true, player, screenWidth, screenHeight, currentTick);
            } else {
                dragStartX = downX;
                dragStartY = downY;
                draggedObject = objectAt(downX, downY, player, screenWidth, screenHeight);
                if (draggedObject == null) {
                    handleClick(downX, downY, false, player, screenWidth, screenHeight, currentTick);
                }
            }
        }
        if (upX != -1 && upY != -1 && draggedObject != null) {
            Object source = draggedObject;
            int target = hotbarIndexAt(upX, upY, screenWidth, screenHeight);
            if (Math.hypot(upX - dragStartX, upY - dragStartY) < 8) {
                handleClick(dragStartX, dragStartY, false, player, screenWidth, screenHeight, currentTick);
            } else if (target >= 0 && source instanceof Item) {
                moveToHotbar((Item) source, target, player);
            } else if (isStoragePoint(upX, upY, screenWidth, screenHeight)) {
                if (source instanceof Item) moveToStorage((Item) source, player);
                else if (source instanceof Ability) moveToAbilityStorage((Ability) source, player);
            } else if (isHeadSlot(upX, upY, screenWidth, screenHeight) && source instanceof Armor
                    && ((Armor) source).getSlot() == ArmorSlot.HEAD) {
                Armor old = player.getHeadArmor();
                removeFromInventory(source, player);
                player.equipHead((Armor) source);
                if (old != null) addObjectToStorage(old);
            } else if (isBodySlot(upX, upY, screenWidth, screenHeight) && source instanceof Armor
                    && ((Armor) source).getSlot() == ArmorSlot.BODY) {
                Armor old = player.getBodyArmor();
                removeFromInventory(source, player);
                player.equipBody((Armor) source);
                if (old != null) addObjectToStorage(old);
            } else if (isAbilitySlot(upX, upY, screenWidth, screenHeight) && source instanceof Ability) {
                int slot = abilitySlotAt(upX, upY, screenWidth, screenHeight);
                Ability[] slots = player.getAbilitySlots();
                Ability old = slots[slot];
                removeFromInventory(source, player);
                slots[slot] = (Ability) source;
                if (old != null) addObjectToStorage(old);
            }
            draggedObject = null;
        }
    }

    private Object objectAt(int x, int y, Player player, int sw, int sh) {
        int px = (sw - 840) / 2, py = (sh - 560) / 2;
        int leftX = px + 24, leftY = py + 75;
        int hotbar = hotbarIndexAt(x, y, sw, sh);
        if (hotbar >= 0) return player.getInventory()[hotbar];
        if (isHeadSlot(x, y, sw, sh)) return player.getHeadArmor();
        if (isBodySlot(x, y, sw, sh)) return player.getBodyArmor();
        if (isAbilitySlot(x, y, sw, sh)) return player.getAbilitySlots()[abilitySlotAt(x, y, sw, sh)];
        int gx = px + 315, gy = py + 115, slotW = 54, pad = 8;
        if (x >= gx && x < gx + 5 * (slotW + pad) && y >= gy && y < gy + 4 * (slotW + pad)) {
            int col = (x - gx) / (slotW + pad), row = (y - gy) / (slotW + pad);
            List<Object> filtered = getFilteredStorage();
            int index = row * 5 + col;
            return index < filtered.size() ? filtered.get(index) : null;
        }
        return null;
    }

    private int hotbarIndexAt(int x, int y, int sw, int sh) {
        int px = (sw - 840) / 2, py = (sh - 560) / 2;
        int leftX = px + 24, leftY = py + 75;
        for (int i = 0; i < 9; i++) {
            int sx = leftX + (i % 5) * 50, sy = leftY + 284 + (i / 5) * 52;
            if (x >= sx && x <= sx + 44 && y >= sy && y <= sy + 44) return i;
        }
        return -1;
    }

    private boolean isStoragePoint(int x, int y, int sw, int sh) {
        int px = (sw - 840) / 2, py = (sh - 560) / 2;
        return x >= px + 315 && x < px + 315 + 5 * 62 && y >= py + 115 && y < py + 115 + 4 * 62;
    }
    private boolean isHeadSlot(int x, int y, int sw, int sh) {
        int px = (sw - 840) / 2, py = (sh - 560) / 2;
        return x >= px + 24 && x <= px + 74 && y >= py + 83 && y <= py + 133;
    }
    private boolean isBodySlot(int x, int y, int sw, int sh) {
        int px = (sw - 840) / 2, py = (sh - 560) / 2;
        return x >= px + 89 && x <= px + 139 && y >= py + 83 && y <= py + 133;
    }
    private boolean isAbilitySlot(int x, int y, int sw, int sh) { return abilitySlotAt(x, y, sw, sh) >= 0; }
    private int abilitySlotAt(int x, int y, int sw, int sh) {
        int px = (sw - 840) / 2, py = (sh - 560) / 2;
        int leftX = px + 24, top = py + 187;
        for (int i = 0; i < 3; i++) {
            int sx = leftX + i * 58;
            if (x >= sx && x <= sx + 50 && y >= top && y <= top + 50) return i;
        }
        return -1;
    }
    private void removeFromInventory(Object object, Player player) {
        storage.remove(object); abilityStorage.remove(object);
        Item[] items = player.getInventory();
        for (int i = 0; i < items.length; i++) if (items[i] == object) items[i] = null;
        Ability[] abilities = player.getAbilitySlots();
        for (int i = 0; i < abilities.length; i++) if (abilities[i] == object) abilities[i] = null;
        if (player.getHeadArmor() == object) player.equipHead(null);
        if (player.getBodyArmor() == object) player.equipBody(null);
    }
    private void addObjectToStorage(Object object) {
        if (object instanceof Item) addItem((Item) object);
        else if (object instanceof Ability) abilityStorage.add((Ability) object);
    }
    private void moveToHotbar(Item item, int index, Player player) {
        Item[] items = player.getInventory();
        Item displaced = items[index];
        if (item == displaced) return;
        if (item instanceof Consumable && displaced instanceof Consumable
                && ((Consumable) item).canStackWith((Consumable) displaced)) {
            removeFromInventory(item, player);
            ((Consumable) displaced).mergeFrom((Consumable) item);
            if (!((Consumable) item).isUsed()) addItem(item);
            return;
        }
        removeFromInventory(item, player);
        items[index] = item;
        if (displaced != null && displaced != item) addObjectToStorage(displaced);
    }
    private void moveToStorage(Item item, Player player) { removeFromInventory(item, player); addItem(item); }
    private void moveToAbilityStorage(Ability ability, Player player) { removeFromInventory(ability, player); abilityStorage.add(ability); }

    private void drawEquippedGear(Graphics2D g2d, Player player, int x, int y) {
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString("ARMOR SLOTS", x, y);

        // Head Armor Slot
        Armor head = player.getHeadArmor();
        drawSlot(g2d, x, y + 8, 50, 50, "HEAD", head != null ? head.getIconColor() : null,
                head != null ? head.getName() : null, head != null ? head.getSpritePath() : null);

        // Body Armor Slot
        Armor body = player.getBodyArmor();
        drawSlot(g2d, x + 65, y + 8, 50, 50, "BODY", body != null ? body.getIconColor() : null,
                body != null ? body.getName() : null, body != null ? body.getSpritePath() : null);

        ArmorSet displayedSet = head != null && head.getArmorSet() != null
                ? head.getArmorSet() : body == null ? null : body.getArmorSet();
        int setPieces = displayedSet == null ? 0 : player.getArmorSetPieceCount(displayedSet);
        ArmorSet activeSet = player.getActiveArmorSet();
        boolean mixedSets = head != null && body != null
                && head.getArmorSet() != null && body.getArmorSet() != null
                && head.getArmorSet() != body.getArmorSet();
        String setStatus;
        if (activeSet != null) {
            setStatus = "SET BONUS: " + activeSet.getDisplayName() + " active - " + activeSet.getBonusDescription();
        } else if (mixedSets) {
            setStatus = "SET BONUS: Equip matching armor pieces";
        } else if (displayedSet != null) {
            setStatus = "SET: " + displayedSet.getDisplayName() + " " + setPieces + "/"
                    + ArmorSet.REQUIRED_PIECES + " - " + displayedSet.getBonusDescription();
        } else {
            setStatus = "SET BONUS: Match 2 pieces to activate";
        }
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2d.setColor(activeSet != null ? new Color(140, 220, 150) : new Color(175, 180, 200));
        g2d.drawString(setStatus, x, y + 76);

        // Abilities
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString("ACTIVE ABILITIES (Q / E / F)", x, y + 104);

        String[] abKeys = {"Q", "E", "F"};
        Ability[] abs = player.getAbilitySlots();
        for (int i = 0; i < 3; i++) {
            Ability a = (abs != null && i < abs.length) ? abs[i] : null;
            drawSlot(g2d, x + i * 58, y + 112, 50, 50, abKeys[i],
                    a != null ? a.getIconColor() : null,
                    a != null ? a.getName() : null, a != null ? a.getSpritePath() : null);
        }

        // Passives
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString("PASSIVES", x, y + 188);

        Ability[] passives = player.getPassiveSlots();
        for (int i = 0; i < 2; i++) {
            Ability p = (passives != null && i < passives.length) ? passives[i] : null;
            drawSlot(g2d, x + i * 58, y + 196, 50, 50, "P" + (i + 1),
                    p != null ? p.getIconColor() : null,
                    p != null ? p.getName() : null, p != null ? p.getSpritePath() : null);
        }

        // Hotbar
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString("HOTBAR SLOTS (1 - 9)", x, y + 272);

        Item[] inv = player.getInventory();
        for (int i = 0; i < 9; i++) {
            int row = i / 5;
            int col = i % 5;
            int sx = x + col * 50;
            int sy = y + 284 + row * 52;
            Item it = (inv != null && i < inv.length) ? inv[i] : null;
            drawSlot(g2d, sx, sy, 44, 44, String.valueOf(i + 1),
                    it != null ? it.getIconColor() : null,
                    it != null ? it.getName() : null, it != null ? it.getSpritePath() : null);
            drawStackCount(g2d, it, sx, sy, 44, 44);
        }
    }

    private void drawStorageSection(Graphics2D g2d, int x, int y, int mouseX, int mouseY) {
        // Tab buttons
        Tab[] tabs = Tab.values();
        int tabX = x;
        for (Tab t : tabs) {
            boolean active = (t == currentTab);
            int tw = 92;
            int th = 26;

            g2d.setColor(active ? new Color(55, 90, 160) : new Color(36, 40, 52));
            g2d.fillRoundRect(tabX, y, tw, th, 8, 8);
            g2d.setColor(active ? Color.WHITE : new Color(175, 180, 200));
            g2d.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 11));
            g2d.drawString(t.label, tabX + 10, y + 17);

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
                String spritePath = null;

                if (obj instanceof Item) {
                    Item it = (Item) obj;
                    col = it.getIconColor();
                    label = it.getName();
                    spritePath = it.getSpritePath();
                } else if (obj instanceof Ability) {
                    Ability ab = (Ability) obj;
                    col = ab.getIconColor();
                    label = ab.getName();
                    spritePath = ab.getSpritePath();
                }

                drawSlot(g2d, sx, sy, slotW, slotH, null, col, label, spritePath);
                drawStackCount(g2d, obj instanceof Item ? (Item) obj : null, sx, sy, slotW, slotH);
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

    private void drawSlot(Graphics2D g2d, int x, int y, int w, int h, String tag, Color fill, String name, String spritePath) {
        g2d.setColor(new Color(30, 33, 44));
        g2d.fillRoundRect(x, y, w, h, 8, 8);
        g2d.setColor(new Color(68, 74, 94));
        g2d.drawRoundRect(x, y, w, h, 8, 8);

        Image sprite = getSprite(spritePath);
        if (sprite != null) {
            int sw = Math.min(w - 10, 32);
            int sh = Math.min(h - 10, 32);
            int sx = x + (w - sw) / 2;
            int sy = y + (h - sh) / 2;
            g2d.drawImage(sprite, sx, sy, sw, sh, null);
        } else if (fill != null) {
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
            g2d.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2d.setColor(new Color(190, 195, 210));
            g2d.drawString(tag, x + 3, y + 10);
        }
    }

    private void drawStackCount(Graphics2D g2d, Item item, int x, int y, int w, int h) {
        if (!(item instanceof Consumable) || ((Consumable) item).getStackCount() <= 1) return;
        String count = String.valueOf(((Consumable) item).getStackCount());
        g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics metrics = g2d.getFontMetrics();
        int badgeWidth = metrics.stringWidth(count) + 8;
        int badgeX = x + w - badgeWidth - 2;
        int badgeY = y + h - 15;
        g2d.setColor(new Color(15, 17, 24, 220));
        g2d.fillRoundRect(badgeX, badgeY, badgeWidth, 13, 6, 6);
        g2d.setColor(Color.WHITE);
        g2d.drawString(count, badgeX + 4, badgeY + 10);
    }

    // ── TOOLTIP RENDERING ───────────────────────────────────────────────────

    private void drawTooltip(Graphics2D g2d, Player player, int mouseX, int mouseY, int px, int py, int screenWidth, int screenHeight) {
        Object hoveredObj = null;
        String actionHint = null;

        // 1. Check storage grid
        int gridX = px + 315;
        int gridY = py + 115;
        int slotW = 54;
        int slotH = 54;
        int pad = 8;
        List<Object> filtered = getFilteredStorage();

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 5; c++) {
                int sx = gridX + c * (slotW + pad);
                int sy = gridY + r * (slotH + pad);
                if (mouseX >= sx && mouseX <= sx + slotW && mouseY >= sy && mouseY <= sy + slotH) {
                    int idx = r * 5 + c;
                    if (idx < filtered.size()) {
                        hoveredObj = filtered.get(idx);
                        actionHint = (hoveredObj instanceof Consumable)
                                ? "Right-Click to Consume | Left-Click to Equip"
                                : "Left-Click to Equip / Move";
                    }
                    break;
                }
            }
        }

        // 2. Check equipped gear on left
        int leftX = px + 24;
        int leftY = py + 75;

        // Head Armor
        if (hoveredObj == null && mouseX >= leftX && mouseX <= leftX + 50 && mouseY >= leftY + 8 && mouseY <= leftY + 58) {
            hoveredObj = player.getHeadArmor();
            actionHint = "Left-Click to Unequip to Storage";
        }
        // Body Armor
        if (hoveredObj == null && mouseX >= leftX + 65 && mouseX <= leftX + 115 && mouseY >= leftY + 8 && mouseY <= leftY + 58) {
            hoveredObj = player.getBodyArmor();
            actionHint = "Left-Click to Unequip to Storage";
        }
        // Abilities Q/E/F
        if (hoveredObj == null) {
            String[] keys = {"Q", "E", "F"};
            Ability[] abs = player.getAbilitySlots();
            for (int i = 0; i < 3; i++) {
                int ax = leftX + i * 58;
                int ay = leftY + 112;
                if (mouseX >= ax && mouseX <= ax + 50 && mouseY >= ay && mouseY <= ay + 50) {
                    if (abs != null && i < abs.length && abs[i] != null) {
                        hoveredObj = abs[i];
                        actionHint = "Active Slot [" + keys[i] + "] (In combat: press " + keys[i] + ")";
                    }
                    break;
                }
            }
        }
        // Passives P1/P2
        if (hoveredObj == null) {
            Ability[] passives = player.getPassiveSlots();
            for (int i = 0; i < 2; i++) {
                int pxSlot = leftX + i * 58;
                int pySlot = leftY + 196;
                if (mouseX >= pxSlot && mouseX <= pxSlot + 50 && mouseY >= pySlot && mouseY <= pySlot + 50) {
                    if (passives != null && i < passives.length && passives[i] != null) {
                        hoveredObj = passives[i];
                        actionHint = "Passive Slot [P" + (i + 1) + "]";
                    }
                    break;
                }
            }
        }
        // Hotbar 1-9
        if (hoveredObj == null) {
            Item[] inv = player.getInventory();
            for (int i = 0; i < 9; i++) {
                int row = i / 5;
                int col = i % 5;
                int sx = leftX + col * 50;
                int sy = leftY + 284 + row * 52;
                if (mouseX >= sx && mouseX <= sx + 44 && mouseY >= sy && mouseY <= sy + 44) {
                    if (inv != null && i < inv.length && inv[i] != null) {
                        hoveredObj = inv[i];
                        actionHint = (hoveredObj instanceof Consumable)
                                ? "Right-Click to Consume | Left-Click to Unequip"
                                : "Hotbar [" + (i + 1) + "] (Click to Unequip to Storage)";
                    }
                    break;
                }
            }
        }

        if (hoveredObj == null) return;

        // Render Tooltip Card
        renderTooltipCard(g2d, player, hoveredObj, actionHint, mouseX, mouseY, screenWidth, screenHeight);
    }

    private void renderTooltipCard(Graphics2D g2d, Player player, Object obj, String actionHint,
                                   int mouseX, int mouseY, int screenWidth, int screenHeight) {
        String title = "Item";
        String badge = "";
        String desc = "";
        Color glowColor = Color.GRAY;

        if (obj instanceof Armor) {
            Armor a = (Armor) obj;
            title = a.getName();
            badge = "ARMOR [" + a.getSlot() + "]  •  " + (int) Math.round(a.getArmorReduction() * 100) + "% DAMAGE REDUCTION";
            desc = a.getDescription();
            ArmorSet armorSet = a.getArmorSet();
            if (armorSet != null) {
                int pieces = player.getArmorSetPieceCount(armorSet);
                boolean active = pieces >= ArmorSet.REQUIRED_PIECES;
                desc += " Set: " + armorSet.getDisplayName() + " Armor. Bonus ("
                        + ArmorSet.REQUIRED_PIECES + " pieces): " + armorSet.getBonusDescription()
                        + ". Equipped: " + pieces + "/" + ArmorSet.REQUIRED_PIECES
                        + (active ? " - active." : " - incomplete.");
            }
            glowColor = new Color(100, 160, 255);
        } else if (obj instanceof Wand) {
            Wand w = (Wand) obj;
            title = w.getName();
            badge = "LEVEL " + w.getLevel() + "  •  " + w.getDamage(player) + " DAMAGE";
            desc = w.getDescription() + " | Fire rate: "
                    + String.format("%.2f attacks/s", w.getAttacksPerSecond())
                    + " | Range: " + w.getEffectiveRange() + " px";
            glowColor = new Color(180, 80, 255);
        } else if (obj instanceof MeleeWeapon) {
            MeleeWeapon mw = (MeleeWeapon) obj;
            title = mw.getName();
            badge = "LEVEL " + mw.getLevel() + "  •  " + mw.getDamage() + " DAMAGE";
            desc = mw.getDescription() + " | Swing speed: "
                    + String.format("%.2f attacks/s", mw.getAttacksPerSecond())
                    + " | Range: " + mw.getAttackRange() + " px";
            glowColor = new Color(255, 215, 0);
        } else if (obj instanceof Consumable) {
            Consumable c = (Consumable) obj;
            title = c.getName();
            badge = "CONSUMABLE  •  " + c.getUsesRemaining()
                    + (c.getUsesRemaining() == 1 ? " USE LEFT  •  " : " USES LEFT  •  ")
                    + c.getStackCount() + " ITEMS";
            desc = c.getDescription();
            glowColor = new Color(255, 75, 75);
        } else if (obj instanceof Ability) {
            Ability ab = (Ability) obj;
            title = ab.getName();
            badge = "ABILITY  •  " + String.format("%.1fs COOLDOWN", ab.getCooldownTicks() / 60.0);
            desc = ab.getDescription();
            glowColor = new Color(255, 200, 60);
        } else if (obj instanceof Item) {
            Item it = (Item) obj;
            title = it.getName();
            desc = it.getDescription();
        }

        // Word-wrap description lines
        List<String> descLines = wrapText(desc, 32);

        int cardW = 280;
        int cardH = 75 + (descLines.size() * 15) + (actionHint != null ? 22 : 0);

        int cardX = mouseX + 16;
        int cardY = mouseY + 16;

        if (cardX + cardW > screenWidth - 15) {
            cardX = mouseX - cardW - 12;
        }
        if (cardY + cardH > screenHeight - 15) {
            cardY = screenHeight - cardH - 15;
        }

        // Background
        g2d.setColor(new Color(16, 18, 26, 248));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
        g2d.setColor(glowColor);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);
        g2d.setStroke(new BasicStroke(1.0f));

        // Title
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, cardX + 12, cardY + 22);

        // Badge pill
        if (!badge.isEmpty()) {
            g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2d.setColor(glowColor);
            g2d.drawString(badge, cardX + 12, cardY + 38);
        }

        // Divider
        g2d.setColor(new Color(50, 55, 70));
        g2d.drawLine(cardX + 12, cardY + 46, cardX + cardW - 12, cardY + 46);

        // Description lines
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2d.setColor(new Color(210, 215, 230));
        int textY = cardY + 62;
        for (String line : descLines) {
            g2d.drawString(line, cardX + 12, textY);
            textY += 15;
        }

        // Action hint
        if (actionHint != null) {
            g2d.setFont(new Font("SansSerif", Font.ITALIC, 10));
            g2d.setColor(new Color(140, 220, 150));
            g2d.drawString(actionHint, cardX + 12, textY + 8);
        }
    }

    private List<String> wrapText(String text, int maxCharsPerLine) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;

        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (current.length() + word.length() + 1 > maxCharsPerLine) {
                if (current.length() > 0) {
                    lines.add(current.toString());
                    current = new StringBuilder();
                }
            }
            if (current.length() > 0) current.append(" ");
            current.append(word);
        }

        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    // ── CLICK HANDLING (EQUIP / UNEQUIP / CONSUME) ──────────────────────────

    public void handleClick(int clickX, int clickY, boolean isRightClick, Player player, int screenWidth, int screenHeight, int currentTick) {
        if (player == null || !player.isInventoryOpen()) return;

        int pw = 840;
        int ph = 560;
        int px = (screenWidth - pw) / 2;
        int py = (screenHeight - ph) / 2;

        // Check tabs
        Tab[] tabs = Tab.values();
        int tabX = px + 315;
        int tabY = py + 75;
        for (Tab t : tabs) {
            if (clickX >= tabX && clickX <= tabX + 92 && clickY >= tabY && clickY <= tabY + 26) {
                currentTab = t;
                return;
            }
            tabX += 92 + 6;
        }

        // Check storage grid clicks
        int gridX = px + 315;
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

                        // If right-clicked a consumable: use and consume immediately!
                        if (isRightClick && obj instanceof Consumable) {
                            Consumable con = (Consumable) obj;
                            con.use(player, currentTick);
                            if (con.isUsed()) {
                                storage.remove(con);
                            }
                            return;
                        }

                        // Left-click actions:
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
                        } else if (obj instanceof Consumable) {
                            Consumable con = (Consumable) obj;
                            int previousCount = con.getStackCount();
                            if (player.addToHotbar(con)) {
                                storage.remove(con);
                            } else if (con.getStackCount() == previousCount) {
                                con.use(player, currentTick);
                                if (con.isUsed()) storage.remove(con);
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
        int leftX = px + 24;
        int leftY = py + 75;

        // Head slot:
        if (clickX >= leftX && clickX <= leftX + 50 && clickY >= leftY + 8 && clickY <= leftY + 58) {
            if (player.getHeadArmor() != null) {
                storage.add(player.getHeadArmor());
                player.equipHead(null);
            }
            return;
        }
        // Body slot:
        if (clickX >= leftX + 65 && clickX <= leftX + 115 && clickY >= leftY + 8 && clickY <= leftY + 58) {
            if (player.getBodyArmor() != null) {
                storage.add(player.getBodyArmor());
                player.equipBody(null);
            }
            return;
        }

        // Hotbar click:
        Item[] inv = player.getInventory();
        for (int i = 0; i < 9; i++) {
            int row = i / 5;
            int col = i % 5;
            int sx = leftX + col * 50;
            int sy = leftY + 284 + row * 52;
            if (clickX >= sx && clickX <= sx + 44 && clickY >= sy && clickY <= sy + 44) {
                if (inv[i] != null) {
                    if (isRightClick && inv[i] instanceof Consumable) {
                        Consumable con = (Consumable) inv[i];
                        con.use(player, currentTick);
                        if (con.isUsed()) {
                            inv[i] = null;
                        }
                    } else {
                        storage.add(inv[i]);
                        inv[i] = null;
                    }
                }
                return;
            }
        }
    }

    public void handleClick(int clickX, int clickY, Player player, int screenWidth, int screenHeight) {
        handleClick(clickX, clickY, false, player, screenWidth, screenHeight, 0);
    }
}
