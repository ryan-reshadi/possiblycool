package Levels.Generation;

/** Generates boss node layouts (delegates to LayoutGenerator for now). */
public final class BossRoomPolicy implements LevelGenPolicy {
    @Override
    public LevelBlueprint generate(GenerationContext ctx) {
        return LayoutGenerator.generateBoss(ctx);
    }
}
