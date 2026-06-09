package Objects.Animations;

import java.util.ArrayList;

public class Animation {

    private int currentTick = -1;
    private ArrayList<AnimationSegment> segments;
    private Runnable onStart;
    private Runnable onEnd;

    //only periodically updated, use totalTicks() for more accuracy, totalTicks calculated on construction and on play
    private int totalTicks;
    public Animation(ArrayList<AnimationSegment> segments, Runnable onStart, Runnable onEnd) {
        this.segments = segments;
        this.onStart = onStart;
        this.onEnd = onEnd;
        this.totalTicks = this.totalTicks();
    }

    public Animation(ArrayList<AnimationSegment> segments) {
        this(segments, null, null);
    }

    public Animation(){
        this(new ArrayList<>(), null, null);
    }

    public void addSegment(AnimationSegment segment) {
        this.segments.add(segment);
    }

    public void addStartAction(Runnable action) {
        this.onStart = action;
    }

    public void addEndAction(Runnable action) {
        this.onEnd = action;
    }

    public void play() {
        if (onStart != null) {
            onStart.run();
        }

        currentTick = 0;
        totalTicks = totalTicks();
    }

    public int totalTicks() {
        int total = 0;
        for (AnimationSegment segment : this.segments) {
            total += segment.getTicks();
        }
        return total;
    }

    public void tick(){
        if (this.segments == null || this.segments.isEmpty()) return;
        if (currentTick < totalTicks && currentTick >= 0) {
            for (AnimationSegment segment : this.segments) {
                if (currentTick == segment.getStartTick()) {
                    segment.play();
                }
                if (currentTick >= segment.getStartTick() && currentTick < segment.getStartTick() + segment.getTicks()) {
                    segment.tickAction();
                }
                if (currentTick == segment.getStartTick() + segment.getTicks()-1) {
                    segment.endAction();
                }
            }
            currentTick++;
        }
        else if (currentTick == totalTicks) {
            currentTick=-1;
            if (onEnd != null) {
                onEnd.run();
            }
        }
    }

    public boolean isPlaying() {
        return currentTick != -1;
    }
}
