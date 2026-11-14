package Objects.PlayerEquipment;

import Objects.PlayerClasses.Player;
import java.awt.Color;

/**
 * Health Potion — single use, restores 40 HP.
 */
public class HealthPotion extends Consumable {

    private static final int HEAL_AMOUNT = 40;

    public HealthPotion() {
        super(1);
        this.name = "Health Potion";
        this.description = "Restores " + HEAL_AMOUNT + " HP immediately when used. Single use.";
        this.iconColor = new Color(220, 53, 69);
        this.spritePath = "images/drops/potion.png";
    }

    @Override
    public void applyEffect(Player player, int currentTick) {
        player.heal(HEAL_AMOUNT);
        System.out.println("Health potion consumed! Healed " + HEAL_AMOUNT + " HP. Current HP: " + player.getHealth() + "/" + player.getMaxHealth());
    }
}
