package Objects.PlayerEquipment;

import java.awt.Color;

public class TankSword extends MeleeWeapon {
    public TankSword() {
        super(80, 60, 30);
        this.name = "Tank Sword";
        this.description = "Heavy broadsword with a wide sweeping arc. 30 DMG.";
        this.iconColor = new Color(180, 180, 190);
    }
}
