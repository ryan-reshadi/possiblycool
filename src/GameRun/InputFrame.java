package GameRun;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class InputFrame {
    private final Set<Integer> heldKeys;
    private final Set<Integer> pressedKeys;
    private final Set<Integer> releasedKeys;
    private final int mouseX;
    private final int mouseY;
    private final int clickXDown;
    private final int clickYDown;
    private final int clickXUp;
    private final int clickYUp;
    private final boolean rightClick;
    private final int scrollDelta;

    InputFrame(Set<Integer> heldKeys, Set<Integer> pressedKeys, Set<Integer> releasedKeys,
               int mouseX, int mouseY, int clickXDown, int clickYDown,
               int clickXUp, int clickYUp, boolean rightClick, int scrollDelta) {
        this.heldKeys = Collections.unmodifiableSet(new HashSet<>(heldKeys));
        this.pressedKeys = Collections.unmodifiableSet(new HashSet<>(pressedKeys));
        this.releasedKeys = Collections.unmodifiableSet(new HashSet<>(releasedKeys));
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.clickXDown = clickXDown;
        this.clickYDown = clickYDown;
        this.clickXUp = clickXUp;
        this.clickYUp = clickYUp;
        this.rightClick = rightClick;
        this.scrollDelta = scrollDelta;
    }

    public Set<Integer> getHeldKeys() {
        return heldKeys;
    }

    public boolean isHeld(int keyCode) {
        return heldKeys.contains(keyCode);
    }

    public boolean wasPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public boolean wasReleased(int keyCode) {
        return releasedKeys.contains(keyCode);
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public int getClickXDown() {
        return clickXDown;
    }

    public int getClickYDown() {
        return clickYDown;
    }

    public int getClickXUp() {
        return clickXUp;
    }

    public int getClickYUp() {
        return clickYUp;
    }

    public boolean hasMousePress() {
        return clickXDown >= 0 && clickYDown >= 0;
    }

    public boolean isRightClick() {
        return rightClick;
    }

    public int getScrollDelta() {
        return scrollDelta;
    }

    InputFrame withoutEdges() {
        return new InputFrame(heldKeys, Collections.emptySet(), Collections.emptySet(),
                mouseX, mouseY, -1, -1, -1, -1, false, 0);
    }
}
