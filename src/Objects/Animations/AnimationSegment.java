package Objects.Animations;

public class AnimationSegment {

    private int totalTicks;
    private Runnable onStart;
    private Runnable onEnd;
    private Runnable onTick;
    private int startTick;

    public AnimationSegment(int startTick, int totalTicks, Runnable onStart, Runnable onTick, Runnable onEnd) {
        this.startTick = startTick;
        this.totalTicks = totalTicks;
        this.onStart = onStart;
        this.onTick = onTick;
        this.onEnd = onEnd;
    }

    public AnimationSegment(int startTick, int totalTicks) {
        this(startTick, totalTicks, null, null, null);
    }

    public AnimationSegment(int startTick, int totalTicks, Runnable onTick) {
        this(startTick, totalTicks, null, onTick, null);
    }

    public void addStartAction(Runnable action) {
        this.onStart = action;
    }

    public void addTickAction(Runnable action) {
        this.onTick = action;
    }

    public void addEndAction(Runnable action) {
        this.onEnd = action;
    }

    public int getTicks() {
        return totalTicks;
    }

    public void play() {
        this.startAction();
    }

    public void startAction() {
        if (onStart != null) {
            onStart.run();
        }
    }

    public void tickAction() {
        if (onTick != null) {
            onTick.run();
        }
    }

    public void endAction() {
        if (onEnd != null) {
            onEnd.run();
        }
    }

    public int getStartTick() {
        return startTick;
    }
}
