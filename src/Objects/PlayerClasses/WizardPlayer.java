package Objects.PlayerClasses;

import Objects.PlayerEquipment.Abilities.WindGustAbility;
import Objects.PlayerEquipment.Abilities.WizardArcaneBurst;
import Objects.PlayerEquipment.Weapons.BasicWand;
import Objects.PlayerEquipment.Ultimates.FocusedArcUltimate;
import java.awt.Color;

public class WizardPlayer extends Player {

    public WizardPlayer(int x, int y) {
        super(x, y, 100);
        this.speed = 3;
        this.rollSpeed = 1.0;
        this.maxRolls = 1;
        this.rolls = 1;
        this.setInnateArmorReduction(0.10);
        this.color = new Color(150, 110, 220);
        this.setImgPath(null);
        giveStartingLoadout();
    }

    private void giveStartingLoadout() {
        this.inventory[0] = new BasicWand();
        this.MainHandIndex = 0;
        this.abilitySlots[0] = new WizardArcaneBurst();
        this.abilitySlots[1] = new WindGustAbility();
        setUltimate(new FocusedArcUltimate());
    }
}
