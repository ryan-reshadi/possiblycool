package Objects.PlayerEquipment;

import Objects.PlayerClasses.WizardPlayer;

public class WizardHat extends Armor {
    public WizardHat() {
        super("Wizard Hat", "Arcane headwear that reduces damage by 5%.", 0.05, ArmorSlot.HEAD, ArmorSet.WIZARD);
        setAllowedClasses(WizardPlayer.class);
    }
}
