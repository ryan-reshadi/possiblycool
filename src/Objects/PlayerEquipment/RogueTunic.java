package Objects.PlayerEquipment;

import Objects.PlayerClasses.RoguePlayer;

public class RogueTunic extends Armor {
    public RogueTunic() {
        super("Rogue Tunic", "Flexible armor that reduces damage by 20%.", 0.20, ArmorSlot.BODY, ArmorSet.ROGUE);
        setAllowedClasses(RoguePlayer.class);
    }
}
