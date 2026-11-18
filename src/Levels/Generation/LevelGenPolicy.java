package Levels.Generation;

public interface LevelGenPolicy {
    LevelBlueprint generate(GenerationContext ctx);
}
