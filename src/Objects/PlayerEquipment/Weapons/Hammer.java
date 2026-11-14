package Objects.PlayerEquipment.Weapons;

import Objects.PlayerEquipment.MeleeWeapon;
import java.awt.Color;

public class Hammer extends MeleeWeapon {

    public Hammer() {
        super(90, 75, 45);
        this.attackAnimationLength = secondsToTicks(0.8); // slow heavy swing
        this.attackDamageDelay = secondsToTicks(0.35);
        this.name = "War Hammer";
        this.description = "Devastating slow smash with high reach. 45 DMG.";
        this.iconColor = new Color(139, 69, 19);
    }
}
