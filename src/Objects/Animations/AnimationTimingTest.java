package Objects.Animations;

import java.util.ArrayList;
import java.util.List;

public class AnimationTimingTest {
    
    static class CallbackEvent {
        String type;
        int tick;
        String segmentName;
        
        CallbackEvent(String type, int tick, String segmentName) {
            this.type = type;
            this.tick = tick;
            this.segmentName = segmentName;
        }
        
        @Override
        public String toString() {
            return String.format("Tick %d: %s - %s", tick, type, segmentName);
        }
    }
    
    public static void main(String[] args) {
        List<CallbackEvent> events = new ArrayList<>();
        int currentTick[] = {0};
        
        // Create segments with timing tracking
        AnimationSegment segment1 = new AnimationSegment(
            0, 3,
            () -> events.add(new CallbackEvent("START", currentTick[0], "Segment1")),
            () -> events.add(new CallbackEvent("TICK", currentTick[0], "Segment1")),
            () -> events.add(new CallbackEvent("END", currentTick[0], "Segment1"))
        );
        
        AnimationSegment segment2 = new AnimationSegment(
            3, 3,
            () -> events.add(new CallbackEvent("START", currentTick[0], "Segment2")),
            () -> events.add(new CallbackEvent("TICK", currentTick[0], "Segment2")),
            () -> events.add(new CallbackEvent("END", currentTick[0], "Segment2"))
        );
        
        Animation animation = new Animation();
        animation.addSegment(segment1);
        animation.addSegment(segment2);
        animation.addStartAction(() -> events.add(new CallbackEvent("ANIM_START", currentTick[0], "Animation")));
        animation.addEndAction(() -> events.add(new CallbackEvent("ANIM_END", currentTick[0], "Animation")));
        
        animation.play();
        
        // Run animation for 7 ticks (covers both segments)
        for (int i = 0; i < 7; i++) {
            currentTick[0] = i;
            animation.tick();
        }
        
        // Print results
        System.out.println("=== Animation Timing Test Results ===\n");
        for (CallbackEvent event : events) {
            System.out.println(event);
        }
        
        // Verify timing
        System.out.println("\n=== Verification ===");
        verifyTiming(events);
    }
    
    private static void verifyTiming(List<CallbackEvent> events) {
        boolean pass = true;
        
        // Check Segment1: starts at 0, ticks at 0,1,2, ends at 2
        if (!hasEventAt(events, "START", "Segment1", 0)) {
            System.out.println("FAIL: Segment1 should start at tick 0");
            pass = false;
        }
        
        int segment1Ticks = countEventsBetween(events, "TICK", "Segment1", 0, 2);
        if (segment1Ticks != 3) {
            System.out.println("FAIL: Segment1 should tick 3 times (ticks 0,1,2), got " + segment1Ticks);
            pass = false;
        }
        
        if (!hasEventAt(events, "END", "Segment1", 2)) {
            System.out.println("FAIL: Segment1 should end at tick 2");
            pass = false;
        }
        
        // Check Segment2: starts at 3, ticks at 3,4,5, ends at 5
        if (!hasEventAt(events, "START", "Segment2", 3)) {
            System.out.println("FAIL: Segment2 should start at tick 3");
            pass = false;
        }
        
        int segment2Ticks = countEventsBetween(events, "TICK", "Segment2", 3, 5);
        if (segment2Ticks != 3) {
            System.out.println("FAIL: Segment2 should tick 3 times (ticks 3,4,5), got " + segment2Ticks);
            pass = false;
        }
        
        if (!hasEventAt(events, "END", "Segment2", 5)) {
            System.out.println("FAIL: Segment2 should end at tick 5");
            pass = false;
        }
        
        // Check Animation: starts at 0, ends at 6
        if (!hasEventAt(events, "ANIM_START", "Animation", 0)) {
            System.out.println("FAIL: Animation should start at tick 0");
            pass = false;
        }
        
        if (!hasEventAt(events, "ANIM_END", "Animation", 6)) {
            System.out.println("FAIL: Animation should end at tick 6");
            pass = false;
        }
        
        if (pass) {
            System.out.println("PASS: All timing checks passed!");
        }
    }
    
    private static boolean hasEventAt(List<CallbackEvent> events, String type, String name, int tick) {
        return events.stream()
            .anyMatch(e -> e.type.equals(type) && e.segmentName.equals(name) && e.tick == tick);
    }
    
    private static int countEventsBetween(List<CallbackEvent> events, String type, String name, int startTick, int endTick) {
        return (int) events.stream()
            .filter(e -> e.type.equals(type) && e.segmentName.equals(name) && e.tick >= startTick && e.tick <= endTick)
            .count();
    }
}
