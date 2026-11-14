package GameRun;

import java.util.HashSet;
import java.util.Set;

/** Collects AWT events and exposes their transitions to a single simulation step. */
public final class InputState {
    private final Set<Integer> heldKeys = new HashSet<>();
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Set<Integer> releasedKeys = new HashSet<>();
    private int mouseX;
    private int mouseY;
    private int clickXDown = -1;
    private int clickYDown = -1;
    private int clickXUp = -1;
    private int clickYUp = -1;
    private boolean rightClick;
    private int scrollDelta;

    public void keyPressed(int keyCode) {
        if (heldKeys.add(keyCode)) {
            pressedKeys.add(keyCode);
        }
    }

    public void keyReleased(int keyCode) {
        if (heldKeys.remove(keyCode)) {
            releasedKeys.add(keyCode);
        }
    }

    public void mouseMoved(int x, int y) {
        mouseX = x;
        mouseY = y;
    }

    public void mousePressed(int x, int y, boolean rightButton) {
        mouseMoved(x, y);
        clickXDown = x;
        clickYDown = y;
        rightClick = rightButton;
    }

    public void mouseReleased(int x, int y) {
        mouseMoved(x, y);
        clickXUp = x;
        clickYUp = y;
    }

    public void mouseWheelMoved(int rotation) {
        scrollDelta += rotation;
    }

    public InputFrame snapshot() {
        InputFrame frame = new InputFrame(heldKeys, pressedKeys, releasedKeys,
                mouseX, mouseY, clickXDown, clickYDown, clickXUp, clickYUp,
                rightClick, scrollDelta);
        pressedKeys.clear();
        releasedKeys.clear();
        clickXDown = -1;
        clickYDown = -1;
        clickXUp = -1;
        clickYUp = -1;
        rightClick = false;
        scrollDelta = 0;
        return frame;
    }

    public void clear() {
        heldKeys.clear();
        pressedKeys.clear();
        releasedKeys.clear();
        clickXDown = -1;
        clickYDown = -1;
        clickXUp = -1;
        clickYUp = -1;
        rightClick = false;
        scrollDelta = 0;
    }
}
