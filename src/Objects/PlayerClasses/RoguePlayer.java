package Objects.PlayerClasses;

import Objects.PlayerEquipment.Abilities.RogueShadowStep;
import Objects.PlayerEquipment.Abilities.TrickstersArrowAbility;
import Objects.PlayerEquipment.Weapons.Dagger;
import Objects.PlayerEquipment.Ultimates.AThousandCutsUltimate;
import java.awt.Color;

public class RoguePlayer extends Player {

    public RoguePlayer(int x, int y) {
        super(x, y, 75);
        this.speed = 5;
        this.rollSpeed = 1.5;
        this.maxRolls = 2;
        this.rolls = 2;
        this.color = new Color(70, 180, 160);
        this.setImgPath(null);
        giveStartingLoadout();
    }

    private void giveStartingLoadout() {
        this.inventory[0] = new Dagger();
        this.MainHandIndex = 0;
        this.abilitySlots[0] = new RogueShadowStep();
        this.abilitySlots[1] = new TrickstersArrowAbility();
        setUltimate(new AThousandCutsUltimate());
    }
}
