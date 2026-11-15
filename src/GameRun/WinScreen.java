package GameRun;

import Objects.PlayerEquipment.Item;
import Objects.PlayerEquipment.MeleeWeapon;
import Objects.PlayerEquipment.Weapons.BasicWand;
import Objects.PlayerEquipment.Weapons.Dagger;
import Objects.PlayerEquipment.Weapons.Hammer;
import Objects.PlayerEquipment.Weapons.Wand;
import Objects.PlayerEquipment.TankSword;
import Objects.PlayerEquipment.PlayerWeapon;
import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import ShapeCore.Buttons.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import GameRun.InputFrame;

public class WinScreen {
    private final Game game;
    private final List<VisualObject> objects = new ArrayList<>();
    private final RewardChest rewardChest;
    private boolean rewardClaimed = false;
    private Item rewardedItem = null;

    public WinScreen(Game game) {
        this.game = game;
        this.rewardChest = new RewardChest(800, 350, game.getPlayer(), this::onChestOpened);
        this.objects.add(rewardChest);
    }

    private void onChestOpened(Item item) {
        this.rewardedItem = item;
        this.rewardClaimed = true;
    }

    public void update(InputFrame input, int tickCount) {
        for (VisualObject obj : objects) {
            if (obj instanceof Button) {
                ((Button) obj).update(input, tickCount);
            } else {
                obj.update(tickCount);
            }
        }

        if (!rewardChest.isOpened() && input.hasMousePress()) {
            int clickX = input.getClickXDown();
            int clickY = input.getClickYDown();
            if (rewardChest.checkCollision(clickX, clickY)) {
                rewardChest.open();
            }
        }
    }

    public void render(Graphics2D g, int tickCount) {
        g.setColor(new Color(20, 25, 35));
        g.fillRect(0, 0, 1920, 1080);

        g.setColor(new Color(100, 200, 100));
        g.setFont(new Font("Serif", Font.BOLD, 72));
        String victoryText = "LEVEL COMPLETE!";
        int textWidth = g.getFontMetrics().stringWidth(victoryText);
        g.drawString(victoryText, (1920 - textWidth) / 2, 200);

        g.setColor(new Color(200, 200, 200));
        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        String subtitleText = "Click the chest to claim your reward";
        int subWidth = g.getFontMetrics().stringWidth(subtitleText);
        g.drawString(subtitleText, (1920 - subWidth) / 2, 260);

        for (VisualObject obj : objects) {
            obj.render(g, tickCount);
        }

        if (rewardClaimed && rewardedItem != null) {
            renderRewardDisplay(g);
        }

        if (rewardClaimed) {
            g.setColor(new Color(150, 150, 150));
            g.setFont(new Font("SansSerif", Font.PLAIN, 24));
            String continueText = "Press ENTER to continue";
            int contWidth = g.getFontMetrics().stringWidth(continueText);
            g.drawString(continueText, (1920 - contWidth) / 2, 900);
        }
    }

    private void renderRewardDisplay(Graphics2D g) {
        int panelX = 600;
        int panelY = 580;
        int panelW = 720;
        int panelH = 200;

        g.setColor(new Color(30, 35, 45, 230));
        g.fillRoundRect(panelX, panelY, panelW, panelH, 15, 15);
        g.setColor(new Color(100, 200, 100));
        g.drawRoundRect(panelX, panelY, panelW, panelH, 15, 15);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        String rewardText = "REWARD CLAIMED!";
        int rw = g.getFontMetrics().stringWidth(rewardText);
        g.drawString(rewardText, panelX + (panelW - rw) / 2, panelY + 45);

        int itemX = panelX + 50;
        int itemY = panelY + 60;
        int itemSize = 80;

        g.setColor(rewardedItem.getIconColor());
        g.fillRoundRect(itemX, itemY, itemSize, itemSize, 10, 10);
        g.setColor(Color.BLACK);
        g.drawRoundRect(itemX, itemY, itemSize, itemSize, 10, 10);

        if (rewardedItem.getSpritePath() != null) {
            Image sprite = GameRun.AssetLoader.loadImage(rewardedItem.getSpritePath());
            if (sprite != null) {
                g.drawImage(sprite, itemX + 5, itemY + 5, itemSize - 10, itemSize - 10, null);
            }
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString(rewardedItem.getName(), itemX + itemSize + 20, itemY + 30);

        g.setColor(new Color(200, 200, 200));
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.drawString(rewardedItem.getDescription(), itemX + itemSize + 20, itemY + 60);

        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        String typeText = rewardedItem instanceof MeleeWeapon ? "Melee Weapon" :
                          rewardedItem instanceof Wand ? "Magic Wand" : "Item";
        g.drawString(typeText, itemX + itemSize + 20, itemY + 90);
    }

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public void handleContinue() {
        if (rewardClaimed) {
            game.continueAfterReward(rewardedItem);
        }
    }

    private void addItemToPlayerHotbar(Objects.PlayerClasses.Player player, Item item) {
        Item[] inventory = player.getInventory();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) {
                inventory[i] = item;
                return;
            }
        }
        // If hotbar is full, the item is lost (or could be added to inventory panel storage in future)
        System.out.println("Hotbar full! Reward item lost: " + item.getName());
    }

    private static class RewardChest extends VisualObject {
        private boolean opened = false;
        private int openAnimationTick = 0;
        private final java.util.function.Consumer<Item> onOpenCallback;
        private final Player player;
        private final Random random = new Random();

        public RewardChest(int x, int y, Player player, java.util.function.Consumer<Item> onOpenCallback) {
            super(x, y, 120, 90, new Color(139, 69, 19));
            this.player = player;
            this.onOpenCallback = onOpenCallback;
        }

        public boolean isOpened() {
            return opened;
        }

        public void open() {
            if (opened) return;
            opened = true;
            openAnimationTick = 0;

            Item reward = generateReward();
            if (onOpenCallback != null) {
                onOpenCallback.accept(reward);
            }
        }

        private Item generateReward() {
            List<java.util.function.Supplier<? extends Item>> eligible = new ArrayList<>();
            eligible.add(Dagger::new);
            eligible.add(Hammer::new);
            eligible.add(TankSword::new);
            eligible.add(BasicWand::new);
            eligible.removeIf(factory -> !factory.get().canBeUsedBy(player));
            if (eligible.isEmpty()) {
                throw new IllegalStateException("No class-appropriate rewards are configured for "
                        + player.getClass().getSimpleName());
            }
            Item reward = eligible.get(random.nextInt(eligible.size())).get();
            if (reward instanceof PlayerWeapon) {
                PlayerWeapon weapon = (PlayerWeapon) reward;
                Item equipped = player.getEquippedItem();
                int level = equipped instanceof PlayerWeapon ? ((PlayerWeapon) equipped).getLevel() : 1;
                weapon.setLevel(Math.max(1, level));
            }
            return reward;
        }

        @Override
        public void update(int tickCount) {
            if (opened) {
                openAnimationTick++;
            }
        }

        @Override
        public void render(Graphics2D g, int tickCount) {
            draw(g);
        }

        @Override
        public void draw(Graphics g) {
            if (!visible) return;

            Graphics2D g2d = (Graphics2D) g;

            if (opened) {
                float progress = Math.min(1.0f, openAnimationTick / 30.0f);

                g2d.setColor(new Color(90, 50, 20));
                g2d.fillRoundRect(x, y + (int)(height * progress), width, (int)(height * (1 - progress)), 10, 10);

                g2d.setColor(new Color(218, 165, 32));
                int goldHeight = (int)(height * 0.3 * progress);
                g2d.fillRoundRect(x + 10, y + height - goldHeight, width - 20, goldHeight, 8, 8);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
                String openText = "OPEN!";
                int tw = g2d.getFontMetrics().stringWidth(openText);
                g2d.drawString(openText, x + (width - tw) / 2, y + height / 2 + 5);
            } else {
                g2d.setColor(new Color(160, 82, 45));
                g2d.fillRoundRect(x, y, width, height, 10, 10);

                g2d.setColor(new Color(218, 165, 32));
                g2d.fillRoundRect(x + width / 2 - 8, y + 10, 16, 16, 5, 5);

                g2d.setColor(Color.BLACK);
                g2d.drawRoundRect(x, y, width, height, 10, 10);

                g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
                String clickText = "CLICK TO OPEN";
                int tw = g2d.getFontMetrics().stringWidth(clickText);
                g2d.drawString(clickText, x + (width - tw) / 2, y - 10);

                g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
                String hintText = "Contains a weapon";
                int hw = g2d.getFontMetrics().stringWidth(hintText);
                g2d.drawString(hintText, x + (width - hw) / 2, y + height + 20);
            }
        }
    }
}
