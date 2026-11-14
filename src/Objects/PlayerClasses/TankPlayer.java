package Objects.PlayerClasses;

import Objects.PlayerEquipment.Abilities.TankShieldBash;
import Objects.PlayerEquipment.TankSword;

public class TankPlayer extends Player {

    public TankPlayer(int x, int y) {
        super(x, y, 150);
        this.speed = 2;
        this.rollSpeed = 0.5;
        this.maxRolls = 1;
        this.rolls = 1;
        this.totalArmorReduction = 0.25; // 25% innate armor
        this.setImgPath("images/rus.jpg");
        giveStartingLoadout();
    }

    private void giveStartingLoadout() {
        this.inventory[0] = new TankSword();
        this.MainHandIndex = 0;
        this.abilitySlots[0] = new TankShieldBash();
    }
}
