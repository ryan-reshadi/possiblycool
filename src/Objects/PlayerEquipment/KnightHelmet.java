package Objects.PlayerEquipment;

import Objects.PlayerClasses.TankPlayer;

public class KnightHelmet extends Armor {
    public KnightHelmet() {
        super("Knight Helmet", "Sturdy head armor that reduces damage by 15%.", 0.15, ArmorSlot.HEAD, ArmorSet.KNIGHT);
        setAllowedClasses(TankPlayer.class);
    }
}
