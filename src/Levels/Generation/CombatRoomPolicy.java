package Levels.Generation;

/** Generates combat ROOM node layouts (delegates to LayoutGenerator for now). */
public final class CombatRoomPolicy implements LevelGenPolicy {
    @Override
    public LevelBlueprint generate(GenerationContext ctx) {
        return LayoutGenerator.generateCombat(ctx);
    }
}
