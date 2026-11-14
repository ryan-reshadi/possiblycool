package GameRun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.UUID;
import java.util.prefs.Preferences;
import Objects.EnemyClasses.BossEnemy;
import Objects.PlayerClasses.RoguePlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class GameProgressionTest {
    private Preferences preferences;

    @AfterEach
    void removeTemporaryPreferences() throws Exception {
        if (preferences != null) {
            Preferences parent = preferences.parent();
            preferences.removeNode();
            parent.flush();
        }
    }

    @Test
    void defeatingLevelOneBossUnlocksSelectorAndReturnsToMenu() {
        Game game = newGame();
        game.player = new RoguePlayer(400, 300);
        game.changeState(GameStates.LEVEL_1);
        BossEnemy boss = game.getCurrentLevel().getEnemies().stream()
                .filter(BossEnemy.class::isInstance)
                .map(BossEnemy.class::cast)
                .findFirst()
                .orElseThrow();
        boss.hit(100_000, boss.getX(), boss.getY(), 0, 0);

        game.update(new InputState().snapshot());

        assertEquals(GameStates.INITIALIZED_1, game.getState());
        assertTrue(game.isLevelSelectorUnlocked());
        game.update(new InputState().snapshot());
        game.openLevelSelector();
        assertEquals(GameStates.LEVEL_SELECT, game.getState());
        assertThrows(IllegalStateException.class, () -> game.chooseLevel("level-2"));
        game.chooseLevel("level-1");
        assertEquals(GameStates.GAME_STARTED_0, game.getState());
    }

    @Test
    void selectorUnlockSurvivesReopeningProgressionStore() {
        ProgressionStore store = newStore();

        assertFalse(store.isLevelSelectorUnlocked());
        store.unlockLevelSelector();

        assertTrue(new ProgressionStore(preferences).isLevelSelectorUnlocked());
    }

    @Test
    void selectorAndLevelChoicesAreBlockedUntilAvailable() {
        Game game = newGame();

        assertThrows(IllegalStateException.class, game::openLevelSelector);
        assertThrows(IllegalStateException.class, () -> game.changeState(GameStates.LEVEL_SELECT));
        assertThrows(IllegalStateException.class, () -> game.chooseLevel("level-1"));
    }

    private Game newGame() {
        return new Game(newStore());
    }

    private ProgressionStore newStore() {
        preferences = Preferences.userRoot().node("possiblycool/tests/" + UUID.randomUUID());
        return new ProgressionStore(preferences);
    }
}
