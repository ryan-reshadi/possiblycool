package Objects.PlayerEquipment.Weapons;

import Objects.PlayerEquipment.MeleeWeapon;
import Objects.PlayerClasses.TankPlayer;
import java.awt.Color;

public class Hammer extends MeleeWeapon {

    public Hammer() {
        super(90, 75, 45);
        setAllowedClasses(TankPlayer.class);
        this.attackAnimationLength = secondsToTicks(0.8);
        this.attackDamageDelay = secondsToTicks(0.35);
        this.knockbackForce = 12.0;
        setCombo(
                move(76, 48, 0.52, 0.23, 0.8, 0.8),
                move(96, 70, 0.72, 0.34, 1.0, 1.0),
                move(112, 104, 0.92, 0.48, 1.35, 1.5)
        );
        this.name = "War Hammer";
        this.description = "Slow three-hit combo that builds into a broad, heavy finishing smash.";
        this.iconColor = new Color(139, 69, 19);
        this.spritePath = "images/drops/hammer.png";
    }
}
