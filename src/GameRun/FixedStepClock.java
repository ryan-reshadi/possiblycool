package GameRun;

public final class FixedStepClock {
    public static final long STEP_NANOS = 1_000_000_000L / 60;
    public static final int MAX_CATCH_UP_STEPS = 5;
    private static final long MAX_ELAPSED_NANOS = STEP_NANOS * MAX_CATCH_UP_STEPS;

    private long accumulatedNanos;

    public int advance(long elapsedNanos) {
        accumulatedNanos += Math.min(Math.max(0, elapsedNanos), MAX_ELAPSED_NANOS);
        int steps = (int) Math.min(accumulatedNanos / STEP_NANOS, MAX_CATCH_UP_STEPS);
        accumulatedNanos -= steps * STEP_NANOS;
        if (accumulatedNanos >= STEP_NANOS) {
            accumulatedNanos %= STEP_NANOS;
        }
        return steps;
    }

    public long getRemainderNanos() {
        return accumulatedNanos;
    }
}
