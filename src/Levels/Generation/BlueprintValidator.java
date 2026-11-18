package Levels.Generation;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BlueprintValidator {
    private BlueprintValidator() {}

    public static boolean isReachable(LevelBlueprint bp) {
        if (bp == null) return false;
        int w = Math.max(1, bp.width);
        int h = Math.max(1, bp.height);
        int step = 20; // grid step
        // Build blocked set by sampling grid cells whose center is inside any wall
        boolean[][] blocked = new boolean[(w / step) + 2][(h / step) + 2];
        for (Rect wall : bp.walls) {
            if (wall == null) continue;
            int x0 = Math.max(0, wall.x / step);
            int y0 = Math.max(0, wall.y / step);
            int x1 = Math.min(blocked.length - 1, (wall.right()) / step);
            int y1 = Math.min(blocked[0].length - 1, (wall.bottom()) / step);
            for (int gx = x0; gx <= x1; gx++) {
                for (int gy = y0; gy <= y1; gy++) {
                    int cx = gx * step + step / 2;
                    int cy = gy * step + step / 2;
                    if (cx >= wall.x && cx < wall.right() && cy >= wall.y && cy < wall.bottom()) {
                        blocked[gx][gy] = true;
                    }
                }
            }
        }
        // Find start cell
        Point spawn = bp.playerSpawn == null ? new Point(300, 500) : bp.playerSpawn;
        int sx = clamp(spawn.x / step, 0, blocked.length - 1);
        int sy = clamp(spawn.y / step, 0, blocked[0].length - 1);
        // If spawn is blocked, try to find nearest unblocked in small radius
        if (blocked[sx][sy]) {
            boolean found = false;
            for (int r = 1; r < 10 && !found; r++) {
                for (int dx = -r; dx <= r && !found; dx++) {
                    for (int dy = -r; dy <= r && !found; dy++) {
                        int nx = sx + dx, ny = sy + dy;
                        if (nx >= 0 && nx < blocked.length && ny >= 0 && ny < blocked[0].length && !blocked[nx][ny]) {
                            sx = nx; sy = ny; found = true;
                        }
                    }
                }
            }
            if (!found) return false;
        }
        // Flood fill
        boolean[][] visited = new boolean[blocked.length][blocked[0].length];
        ArrayDeque<int[]> q = new ArrayDeque<>();
        q.add(new int[]{sx, sy});
        visited[sx][sy] = true;
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            for (int[] d : dirs) {
                int nx = cur[0] + d[0], ny = cur[1] + d[1];
                if (nx < 0 || nx >= blocked.length || ny < 0 || ny >= blocked[0].length) continue;
                if (visited[nx][ny] || blocked[nx][ny]) continue;
                visited[nx][ny] = true;
                q.add(new int[]{nx, ny});
            }
        }
        // Check all rooms are reachable (at least one cell inside room is visited)
        for (Rect room : bp.rooms) {
            if (room == null) continue;
            if (!isRoomReachable(room, step, visited, blocked.length, blocked[0].length)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isRoomReachable(Rect room, int step, boolean[][] visited, int gw, int gh) {
        int x0 = Math.max(0, room.x / step);
        int y0 = Math.max(0, room.y / step);
        int x1 = Math.min(gw - 1, (room.right() - 1) / step);
        int y1 = Math.min(gh - 1, (room.bottom() - 1) / step);
        for (int gx = x0; gx <= x1; gx++) {
            for (int gy = y0; gy <= y1; gy++) {
                if (visited[gx][gy]) return true;
            }
        }
        return false;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
