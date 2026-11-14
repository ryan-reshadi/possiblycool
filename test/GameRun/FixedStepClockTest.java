package GameRun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class FixedStepClockTest {
    @Test
    void advancesOnlyAfterOneFixedInterval() {
        FixedStepClock clock = new FixedStepClock();
        assertEquals(0, clock.advance(FixedStepClock.STEP_NANOS - 1));
        assertEquals(1, clock.advance(1));
        assertEquals(0, clock.getRemainderNanos());
    }

    @Test
    void boundsCatchUpAndDropsExcessWholeSteps() {
        FixedStepClock clock = new FixedStepClock();
        assertEquals(FixedStepClock.MAX_CATCH_UP_STEPS,
                clock.advance(FixedStepClock.STEP_NANOS * 20));
        assertTrue(clock.getRemainderNanos() < FixedStepClock.STEP_NANOS);
    }
}
