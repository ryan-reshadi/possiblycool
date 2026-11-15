package GameRun;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Draws the current generated route and accepts clicks only on reachable nodes. */
final class RouteMapScreen {
    private final Game game;
    RouteMapScreen(Game game) { this.game = game; }

    void update(InputFrame input) {
        if (!input.hasMousePress()) return;
        RunRoute route = game.getRunRoute();
        if (route == null) return;
        for (RunRoute.Node node : route.getReachableNodes()) {
            int[] p = position(route, node);
            if (input.getClickXDown() >= p[0] - 72 && input.getClickXDown() <= p[0] + 72
                    && input.getClickYDown() >= p[1] - 24 && input.getClickYDown() <= p[1] + 24) {
                game.enterRouteNode(node.id);
                return;
            }
        }
    }

    void render(Graphics2D g) {
        RunRoute route = game.getRunRoute();
        if (route == null) return;
        g.setColor(new Color(18, 20, 29));
        g.fillRect(0, 0, Viewport.WIDTH, Viewport.HEIGHT);
        g.setFont(new Font("SansSerif", Font.BOLD, 34));
        g.setColor(Color.WHITE);
        g.drawString("CHOOSE YOUR ROUTE", 40, 55);
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.setColor(new Color(255, 215, 100));
        g.drawString("Coins: " + game.getPlayerCoins(), 1650, 50);

        for (RunRoute.Node node : route.getNodes()) {
            int[] from = position(route, node);
            for (String id : node.next) {
                RunRoute.Node next = route.getNode(id);
                if (next == null) continue;
                int[] to = position(route, next);
                g.setColor(new Color(90, 100, 125, 150));
                g.setStroke(new BasicStroke(3));
                g.drawLine(from[0], from[1], to[0], to[1]);
            }
        }

        Set<String> reachable = new HashSet<>();
        for (RunRoute.Node n : route.getReachableNodes()) reachable.add(n.id);
        for (RunRoute.Node node : route.getNodes()) {
            int[] p = position(route, node);
            boolean active = reachable.contains(node.id);
            Color fill = node.type == RunRoute.Type.SHOP ? new Color(175, 125, 38)
                    : node.type == RunRoute.Type.BOSS ? new Color(145, 45, 55)
                    : node.type == RunRoute.Type.TUTORIAL ? new Color(55, 120, 110)
                    : new Color(62, 78, 110);
            if (!active) fill = new Color(fill.getRed() / 2, fill.getGreen() / 2, fill.getBlue() / 2);
            g.setColor(fill);
            g.fill(new RoundRectangle2D.Double(p[0] - 72, p[1] - 24, 144, 48, 14, 14));
            g.setColor(active ? Color.WHITE : new Color(130, 130, 140));
            g.setStroke(new BasicStroke(active ? 2 : 1));
            g.draw(new RoundRectangle2D.Double(p[0] - 72, p[1] - 24, 144, 48, 14, 14));
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            String title = node.title;
            int width = g.getFontMetrics().stringWidth(title);
            g.drawString(title, p[0] - width / 2, p[1] + 5);
        }
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.setColor(new Color(175, 180, 195));
        g.drawString("Select a lit node to enter it. Shops let you spend coins on class gear and powers.", 40, 975);
    }

    private static int[] position(RunRoute route, RunRoute.Node node) {
        int x;
        if (node.type == RunRoute.Type.TUTORIAL || node.type == RunRoute.Type.BOSS) x = Viewport.WIDTH / 2;
        else {
            int layerCount = 0, index = 0;
            for (RunRoute.Node candidate : route.getNodes()) {
                if (candidate.stage == node.stage) {
                    if (candidate.id.equals(node.id)) index = layerCount;
                    layerCount++;
                }
            }
            x = Viewport.WIDTH * (index + 1) / (layerCount + 1);
        }
        int y = 900 - node.stage * 80;
        return new int[] {x, Math.max(100, y)};
    }
}
