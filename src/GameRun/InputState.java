package GameRun;

import java.util.HashSet;
import java.util.Set;

/** Tracks held keys and exposes one-shot press events for a game frame. */
public final class InputState {
    private final Set<Integer> previousKeys = new HashSet<>();
    private final Set<Integer> pressedThisFrame = new HashSet<>();

    /** Updates key transitions. Call once at the start of each game frame. */
    public void update(Set<Integer> heldKeys) {
        pressedThisFrame.clear();
        if (heldKeys == null) {
            previousKeys.clear();
            return;
        }

        for (int keyCode : heldKeys) {
            if (!previousKeys.contains(keyCode)) {
                pressedThisFrame.add(keyCode);
            }
        }

        previousKeys.clear();
        previousKeys.addAll(heldKeys);
    }

    /** Returns true only on the frame a key changes from released to held. */
    public boolean wasPressed(int keyCode) {
        return pressedThisFrame.contains(keyCode);
    }
}
