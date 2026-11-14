package Objects.PlayerEquipment;

import Objects.PlayerClasses.RoguePlayer;

public class RogueHood extends Armor {
    public RogueHood() {
        super("Rogue Hood", "Light head armor that reduces damage by 10%.", 0.10, ArmorSlot.HEAD, ArmorSet.ROGUE);
        setAllowedClasses(RoguePlayer.class);
    }
}
