package GameRun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ViewportTest {
    @Test
    void preservesAspectRatioAndMapsCoordinatesToLogicalViewport() {
        assertEquals(0.5, Viewport.scaleFor(950, 1000), 0.0001);
        assertEquals(250, Viewport.offsetYFor(950, 1000));
        assertEquals(950, Viewport.toLogicalX(475, 950, 1000));
        assertEquals(450, Viewport.toLogicalY(475, 950, 1000));
    }
}
