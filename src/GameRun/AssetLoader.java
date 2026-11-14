package GameRun;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.imageio.ImageIO;

public final class AssetLoader {
    private static final ConcurrentMap<String, BufferedImage> IMAGES = new ConcurrentHashMap<>();

    private AssetLoader() {
    }

    public static BufferedImage loadImage(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Image resource path must not be blank.");
        }
        String resourcePath = path.startsWith("/") ? path.substring(1) : path;
        return IMAGES.computeIfAbsent(resourcePath, AssetLoader::readImage);
    }

    private static BufferedImage readImage(String path) {
        ClassLoader classLoader = AssetLoader.class.getClassLoader();
        try {
            InputStream resource = classLoader.getResourceAsStream(path);
            if (resource == null) {
                Path file = Path.of(path);
                if (!Files.isRegularFile(file)) {
                    throw new IllegalArgumentException(
                            "Image resource not found on the classpath or relative to the working directory: " + path);
                }
                resource = Files.newInputStream(file);
            }

            try (InputStream stream = resource) {
                BufferedImage image = ImageIO.read(stream);
                if (image == null) {
                    throw new IllegalArgumentException("Unsupported or invalid image resource: " + path);
                }
                return image;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read image resource: " + path, e);
        }
    }
}
