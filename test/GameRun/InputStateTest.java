package GameRun;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class InputStateTest {
    @Test
    void capturesKeyEdgesOnceAndClearsHeldKeysOnFocusLoss() {
        InputState state = new InputState();
        state.keyPressed(65);
        state.keyPressed(65);

        InputFrame first = state.snapshot();
        assertTrue(first.isHeld(65));
        assertTrue(first.wasPressed(65));
        assertFalse(state.snapshot().wasPressed(65));

        state.clear();
        InputFrame afterFocusLoss = state.snapshot();
        assertFalse(afterFocusLoss.isHeld(65));
        assertFalse(afterFocusLoss.wasPressed(65));
    }

    @Test
    void aggregatesPointerAndWheelEventsUntilTheNextSimulationStep() {
        InputState state = new InputState();
        state.mouseMoved(100, 200);
        state.mousePressed(100, 200, false);
        state.mouseReleased(105, 205);
        state.mouseWheelMoved(1);
        state.mouseWheelMoved(2);

        InputFrame frame = state.snapshot();
        assertTrue(frame.hasMousePress());
        assertTrue(frame.getClickXUp() == 105);
        assertTrue(frame.getMouseY() == 205);
        assertTrue(frame.getScrollDelta() == 3);

        InputFrame next = state.snapshot();
        assertFalse(next.hasMousePress());
        assertTrue(next.getScrollDelta() == 0);
    }

    @Test
    void catchUpFramesRetainHeldKeysButConsumeEdgesOnlyOnce() {
        InputState state = new InputState();
        state.keyPressed(68);
        InputFrame first = state.snapshot();
        InputFrame catchUp = first.withoutEdges();

        assertTrue(first.wasPressed(68));
        assertTrue(catchUp.isHeld(68));
        assertFalse(catchUp.wasPressed(68));
    }
}
