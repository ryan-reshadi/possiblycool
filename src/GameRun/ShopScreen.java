package GameRun;

import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Armor;
import Objects.PlayerEquipment.Item;
import Objects.PlayerEquipment.KnightChestplate;
import Objects.PlayerEquipment.KnightHelmet;
import Objects.PlayerEquipment.RogueHood;
import Objects.PlayerEquipment.RogueTunic;
import Objects.PlayerEquipment.TankSword;
import Objects.PlayerEquipment.Ultimate;
import Objects.PlayerEquipment.WizardHat;
import Objects.PlayerEquipment.WizardRobe;
import Objects.PlayerEquipment.Weapons.BasicWand;
import Objects.PlayerEquipment.Weapons.Dagger;
import Objects.PlayerEquipment.Weapons.Hammer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class ShopScreen {
    private final Game game;
    ShopScreen(Game game) { this.game = game; }

    void ensureStock(RunRoute.Node node) {
        if (!node.shopStock.isEmpty()) return;
        Player player = game.getPlayer();
        Random random = new Random(game.getRunRoute().getSeed() ^ node.id.hashCode());
        List<Item> gear = gearFor(player);
        for (int i = 0; i < 2; i++) addOffer(node, gear.get(random.nextInt(gear.size())));
        Ultimate.Type alternate = Ultimate.alternateFor(player);
        if (!game.ownsUltimate(alternate.getId())) node.shopStock.add(ultimateOffer(alternate));
        else addOffer(node, gear.get(random.nextInt(gear.size())));
    }

    private void addOffer(RunRoute.Node node, Item item) {
        int level = item instanceof Armor ? ((Armor) item).getLevel()
                : item instanceof Objects.PlayerEquipment.PlayerWeapon
                    ? ((Objects.PlayerEquipment.PlayerWeapon) item).getLevel() : 1;
        java.util.Map<String, Object> offer = ProgressionStore.serializeItem(item);
        offer.put("price", 30 + 20 * Math.max(1, level));
        offer.put("offerId", item.getClass().getSimpleName() + "-" + node.shopStock.size());
        node.shopStock.add(offer);
    }

    private java.util.Map<String, Object> ultimateOffer(Ultimate.Type type) {
        java.util.Map<String, Object> offer = ProgressionStore.serializeItem(new Ultimate(type));
        offer.put("price", 120);
        offer.put("offerId", "ultimate-" + type.getId());
        return offer;
    }

    private List<Item> gearFor(Player player) {
        List<Item> items = new ArrayList<>();
        if (player instanceof Objects.PlayerClasses.RoguePlayer) {
            items.add(new RogueHood()); items.add(new RogueTunic()); items.add(new Dagger());
        } else if (player instanceof Objects.PlayerClasses.TankPlayer) {
            items.add(new KnightHelmet()); items.add(new KnightChestplate()); items.add(new Hammer()); items.add(new TankSword());
        } else {
            items.add(new WizardHat()); items.add(new WizardRobe()); items.add(new BasicWand());
        }
        return items;
    }

    void update(InputFrame input, RunRoute.Node node) {
        if (!input.hasMousePress()) return;
        int x = input.getClickXDown(), y = input.getClickYDown();
        for (int i = 0; i < node.shopStock.size(); i++) {
            int cardX = 150 + i * 535;
            if (x >= cardX && x <= cardX + 480 && y >= 300 && y <= 630) {
                game.buyShopOffer(node, node.shopStock.get(i));
                return;
            }
        }
        if (x >= 780 && x <= 1120 && y >= 790 && y <= 865) game.leaveShop(node);
    }

    void render(Graphics2D g, RunRoute.Node node) {
        g.setColor(new Color(24, 22, 25)); g.fillRect(0, 0, Viewport.WIDTH, Viewport.HEIGHT);
        g.setColor(Color.WHITE); g.setFont(new Font("Serif", Font.BOLD, 48)); g.drawString("TRAVELING SHOP", 720, 130);
        g.setColor(new Color(255, 215, 100)); g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString("Coins: " + game.getPlayerCoins(), 830, 190);
        for (int i = 0; i < node.shopStock.size(); i++) {
            java.util.Map<String, Object> offer = node.shopStock.get(i);
            int x = 150 + i * 535;
            boolean bought = node.purchased.contains(String.valueOf(offer.get("offerId")));
            g.setColor(bought ? new Color(45, 55, 50) : new Color(42, 45, 58));
            g.fillRoundRect(x, 300, 480, 330, 18, 18);
            g.setColor(new Color(180, 150, 90)); g.drawRoundRect(x, 300, 480, 330, 18, 18);
            g.setColor(Color.WHITE); g.setFont(new Font("SansSerif", Font.BOLD, 25));
            g.drawString(bought ? "PURCHASED" : offerName(offer), x + 28, 360);
            g.setFont(new Font("SansSerif", Font.PLAIN, 18)); g.setColor(new Color(210, 215, 230));
            g.drawString("Price: " + offer.get("price") + " coins", x + 28, 410);
            g.drawString("Click to buy", x + 28, 575);
        }
        g.setColor(new Color(75, 130, 95)); g.fillRoundRect(780, 790, 340, 75, 16, 16);
        g.setColor(Color.WHITE); g.setFont(new Font("SansSerif", Font.BOLD, 24)); g.drawString("Leave Shop", 880, 835);
    }

    private String offerName(java.util.Map<String, Object> offer) {
        Item item = ProgressionStore.deserializeShopItem(offer);
        return item == null ? "Unavailable item" : item.getName();
    }
}
