package Objects.PlayerEquipment;

import Objects.PlayerClasses.TankPlayer;

public class KnightChestplate extends Armor {
    public KnightChestplate() {
        super("Knight Chestplate", "Heavy plate armor that reduces damage by 30%.", 0.30, ArmorSlot.BODY, ArmorSet.KNIGHT);
        setAllowedClasses(TankPlayer.class);
    }
}
