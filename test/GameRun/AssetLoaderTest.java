package GameRun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class AssetLoaderTest {
    @Test
    void loadsImagesFromClasspathAndReportsMissingResources() {
        assertNotNull(AssetLoader.loadImage("images/rus.jpg"));
        assertNotNull(AssetLoader.loadImage("/images/wizard-ball.png"));
        assertThrows(IllegalArgumentException.class, () -> AssetLoader.loadImage("images/missing.png"));
    }

    @Test
    void loadsImagesRelativeToWorkingDirectoryWhenTheyAreNotClasspathResources() throws IOException {
        Path directory = Path.of("target", "asset-loader-test-" + UUID.randomUUID());
        Path imagePath = directory.resolve("fallback.png");
        Files.createDirectories(directory);
        try {
            ImageIO.write(new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB), "png", imagePath.toFile());

            BufferedImage image = AssetLoader.loadImage(imagePath.toString());

            assertEquals(3, image.getWidth());
            assertEquals(2, image.getHeight());
        } finally {
            Files.deleteIfExists(imagePath);
            Files.deleteIfExists(directory);
        }
    }
}
