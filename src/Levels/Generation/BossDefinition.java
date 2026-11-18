package Levels.Generation;

public final class BossDefinition {
    public final String id;
    public final int baseHealth;
    public final int healthPerStage;

    public BossDefinition(String id, int baseHealth, int healthPerStage) {
        this.id = id == null ? "boss" : id;
        this.baseHealth = baseHealth;
        this.healthPerStage = healthPerStage;
    }
}
