package Objects.PlayerEquipment;

public enum ArmorSet {
    KNIGHT("Knight", 0.05, 0, 0.0, "+5% damage reduction"),
    ROGUE("Rogue", 0.0, 1, 0.0, "+1 movement speed"),
    WIZARD("Wizard", 0.0, 0, 0.15, "+15% spell damage");

    public static final int REQUIRED_PIECES = 2;

    private final String displayName;
    private final double bonusArmorReduction;
    private final int bonusMovementSpeed;
    private final double bonusSpellDamage;
    private final String bonusDescription;

    ArmorSet(String displayName, double bonusArmorReduction, int bonusMovementSpeed,
             double bonusSpellDamage, String bonusDescription) {
        this.displayName = displayName;
        this.bonusArmorReduction = bonusArmorReduction;
        this.bonusMovementSpeed = bonusMovementSpeed;
        this.bonusSpellDamage = bonusSpellDamage;
        this.bonusDescription = bonusDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBonusArmorReduction() {
        return bonusArmorReduction;
    }

    public int getBonusMovementSpeed() {
        return bonusMovementSpeed;
    }

    public double getBonusSpellDamage() {
        return bonusSpellDamage;
    }

    public String getBonusDescription() {
        return bonusDescription;
    }
}
