package GameRun;

import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class ProgressionStore {
    private static final String LEVEL_SELECTOR_UNLOCKED = "levelSelectorUnlocked";
    private final Preferences preferences;

    public ProgressionStore() {
        this(Preferences.userNodeForPackage(ProgressionStore.class));
    }

    ProgressionStore(Preferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences");
    }

    public boolean isLevelSelectorUnlocked() {
        return preferences.getBoolean(LEVEL_SELECTOR_UNLOCKED, false);
    }

    public void unlockLevelSelector() {
        preferences.putBoolean(LEVEL_SELECTOR_UNLOCKED, true);
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            throw new IllegalStateException("Could not save level progression.", e);
        }
    }
}
