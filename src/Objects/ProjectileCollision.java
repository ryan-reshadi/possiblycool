package Objects;

import Objects.Terrain.Border;
import Objects.Terrain.BossDoor;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public final class ProjectileCollision {
    private ProjectileCollision() {
    }

    public static boolean hitsWall(double startX, double startY, double endX, double endY,
                                   int projectileWidth, int projectileHeight,
                                   List<? extends VisualObject> walls) {
        if (walls == null) return false;
        Line2D path = new Line2D.Double(startX, startY, endX, endY);

        for (VisualObject object : walls) {
            if (!(object instanceof Border)) continue;
            if (object instanceof BossDoor && !((BossDoor) object).isLocked()) continue;

            Border wall = (Border) object;
            Rectangle2D expandedWall = new Rectangle2D.Double(
                    wall.getX() - projectileWidth / 2.0,
                    wall.getY() - projectileHeight / 2.0,
                    wall.getWidth() + projectileWidth,
                    wall.getHeight() + projectileHeight);
            if (expandedWall.contains(startX, startY) || expandedWall.intersectsLine(path)) {
                return true;
            }
        }
        return false;
    }
}
