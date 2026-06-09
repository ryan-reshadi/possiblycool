package Objects.Animations;

public class AnimationsTest {
    public static void main(String[] args) {
        AnimationSegment segment1 = new AnimationSegment(0, 5, () -> System.out.println("Segment 1 start"), () -> System.out.println("Segment 1 tick"), () -> System.out.println("Segment 1 end"));
        AnimationSegment segment2 = new AnimationSegment(5, 5, () -> System.out.println("Segment 2 start"), () -> System.out.println("Segment 2 tick"), () -> System.out.println("Segment 2 end"));

        Animation animation = new Animation();
        animation.addSegment(segment1);
        animation.addSegment(segment2);
        animation.addStartAction(() -> System.out.println("Animation start"));
        animation.addEndAction(() -> System.out.println("Animation end"));

        animation.play();

        for (int i = 0; i < 12; i++) {
            animation.tick();
        }
    }
}
