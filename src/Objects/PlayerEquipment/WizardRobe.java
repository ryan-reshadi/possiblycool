package Objects.PlayerEquipment;

import Objects.PlayerClasses.WizardPlayer;

public class WizardRobe extends Armor {
    public WizardRobe() {
        super("Wizard Robe", "Enchanted cloth that reduces damage by 10%.", 0.10, ArmorSlot.BODY, ArmorSet.WIZARD);
        setAllowedClasses(WizardPlayer.class);
    }
}
