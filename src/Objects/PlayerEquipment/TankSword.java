package Objects.PlayerEquipment;

import java.awt.Color;
import Objects.PlayerClasses.TankPlayer;

public class TankSword extends MeleeWeapon {
    public TankSword() {
        super(80, 60, 30);
        setAllowedClasses(TankPlayer.class);
        this.knockbackForce = 8.0;
        setCombo(
                move(72, 44, 0.32, 0.12, 0.85, 0.8),
                move(88, 62, 0.42, 0.18, 1.0, 1.0),
                move(104, 88, 0.54, 0.25, 1.25, 1.3)
        );
        this.name = "Tank Sword";
        this.description = "Three-hit broadsword combo that builds from a quick cut into a wide finisher.";
        this.iconColor = new Color(180, 180, 190);
        this.spritePath = "images/drops/sword.png";
    }
}
