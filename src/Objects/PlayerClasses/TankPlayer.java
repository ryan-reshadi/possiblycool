package Objects.PlayerClasses;

import Objects.PlayerEquipment.Abilities.RallyBarricadeAbility;
import Objects.PlayerEquipment.Abilities.TankShieldBash;
import Objects.PlayerEquipment.TankSword;
import Objects.PlayerEquipment.Ultimates.BattleCryUltimate;
import java.awt.Color;

public class TankPlayer extends Player {

    public TankPlayer(int x, int y) {
        super(x, y, 150);
        this.speed = 2;
        this.rollSpeed = 0.5;
        this.maxRolls = 1;
        this.rolls = 1;
        this.setInnateArmorReduction(0.25);
        this.color = new Color(190, 170, 120);
        this.setImgPath(null);
        giveStartingLoadout();
    }

    private void giveStartingLoadout() {
        this.inventory[0] = new TankSword();
        this.MainHandIndex = 0;
        this.abilitySlots[0] = new TankShieldBash();
        this.abilitySlots[1] = new RallyBarricadeAbility();
        setUltimate(new BattleCryUltimate());
    }
}
