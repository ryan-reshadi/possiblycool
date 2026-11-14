package Objects.PlayerEquipment.Weapons;

import Objects.PlayerEquipment.MeleeWeapon;
import java.awt.Color;

public class Dagger extends MeleeWeapon {

    public Dagger() {
        super(55, 45, 18);
        this.attackAnimationLength = secondsToTicks(0.25); // very fast swing
        this.attackDamageDelay = secondsToTicks(0.08);
        this.name = "Rogue Dagger";
        this.description = "Rapid close-range stab. 18 DMG.";
        this.iconColor = new Color(220, 220, 100);
    }
}
