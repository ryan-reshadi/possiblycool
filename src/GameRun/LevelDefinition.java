package GameRun;

import Levels.BaseLevel;
import Objects.PlayerClasses.Player;
import java.util.Objects;
import java.util.function.Function;

public final class LevelDefinition {
    private final String id;
    private final String title;
    private final Function<Player, BaseLevel> factory;

    private LevelDefinition(String id, String title, Function<Player, BaseLevel> factory) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = Objects.requireNonNull(title, "title");
        this.factory = factory;
    }

    public static LevelDefinition playable(String id, String title, Function<Player, BaseLevel> factory) {
        return new LevelDefinition(id, title, Objects.requireNonNull(factory, "factory"));
    }

    public static LevelDefinition unavailable(String id, String title) {
        return new LevelDefinition(id, title, null);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAvailable() {
        return factory != null;
    }

    public BaseLevel create(Player player) {
        if (factory == null) {
            throw new IllegalStateException("Level is not available: " + id);
        }
        return Objects.requireNonNull(factory.apply(player), "level factory result");
    }
}
