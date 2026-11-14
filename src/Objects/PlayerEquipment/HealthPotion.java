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
        this.description = "Restores " + HEAL_AMOUNT + " HP. Single use.";
        this.iconColor = new Color(220, 53, 69);
    }

    @Override
    public void applyEffect(Player player, int currentTick) {
        player.heal(HEAL_AMOUNT);
    }
}
