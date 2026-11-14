package Objects.PlayerClasses;

import Objects.PlayerEquipment.Abilities.WizardArcaneBurst;
import Objects.PlayerEquipment.Weapons.BasicWand;

public class WizardPlayer extends Player {

    public WizardPlayer(int x, int y) {
        super(x, y, 100);
        this.speed = 3;
        this.rollSpeed = 1.0;
        this.maxRolls = 1;
        this.rolls = 1;
        this.totalArmorReduction = 0.10; // 10% innate protection
        this.setImgPath("images/rus.jpg");
        giveStartingLoadout();
    }

    private void giveStartingLoadout() {
        this.inventory[0] = new BasicWand();
        this.MainHandIndex = 0;
        this.abilitySlots[0] = new WizardArcaneBurst();
    }
}
