package GameRun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

class GameRenderingTest {
    @Test
    void renderingDoesNotAdvanceSimulation() {
        Game game = new Game();
        game.update(new InputState().snapshot());
        int tickAfterUpdate = game.getTickCount();

        BufferedImage image = new BufferedImage(Viewport.WIDTH, Viewport.HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            game.render(graphics);
            game.render(graphics);
        } finally {
            graphics.dispose();
        }

        assertEquals(tickAfterUpdate, game.getTickCount());
    }
}
