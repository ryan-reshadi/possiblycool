package Objects.PlayerEquipment.Weapons;

import Objects.PlayerEquipment.MeleeWeapon;
import Objects.PlayerClasses.RoguePlayer;
import java.awt.Color;

public class Dagger extends MeleeWeapon {

    public Dagger() {
        setAllowedClasses(RoguePlayer.class);
        super(55, 45, 18);
        this.attackAnimationLength = secondsToTicks(0.25);
        this.attackDamageDelay = secondsToTicks(0.08);
        this.knockbackForce = 3.0;
        setCombo(
                move(75, 20, 0.20, 0.07, 1, 0.75),
                move(75, 20, 0.20, 0.07, 1, 0.75),
                move(60, 70, 0.27, 0.10, 0.8, 1.0),
                move(50, 10, 0.15, 0.05, 1.3, 1.35)
        );
        this.name = "Rogue Dagger";
        this.description = "Fast three-hit combo that builds from quick stabs into a wide finishing slash.";
        this.iconColor = new Color(220, 220, 100);
        this.spritePath = "images/drops/dagger.png";
    }
}
