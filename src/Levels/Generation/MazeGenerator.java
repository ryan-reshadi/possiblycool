package Levels.Generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/** Generates a simple perfect-ish maze (recursive backtracker) and returns internal wall rects. */
public final class MazeGenerator {
    private MazeGenerator() {}

    private static final class Cell {
        boolean visited = false;
        boolean wallTop = true, wallRight = true, wallBottom = true, wallLeft = true;
    }

    public static List<Rect> generateMazeWalls(Rect area, int cellSize, Random rng) {
        List<Rect> walls = new ArrayList<>();
        if (area == null || area.w < cellSize * 4 || area.h < cellSize * 4) return walls;
        int cols = Math.max(4, area.w / cellSize);
        int rows = Math.max(4, area.h / cellSize);
        Cell[][] grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) grid[r][c] = new Cell();

        // Recursive backtracker (iterative with stack)
        Stack<int[]> stack = new Stack<>();
        int sr = rows / 2, sc = cols / 2;
        grid[sr][sc].visited = true;
        stack.push(new int[]{sr, sc});
        int[][] dirs = {{-1,0,0},{0,1,1},{1,0,2},{0,-1,3}};
        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            int r = cur[0], c = cur[1];
            List<Integer> neighbors = new ArrayList<>();
            if (r > 0 && !grid[r-1][c].visited) neighbors.add(0);
            if (c < cols-1 && !grid[r][c+1].visited) neighbors.add(1);
            if (r < rows-1 && !grid[r+1][c].visited) neighbors.add(2);
            if (c > 0 && !grid[r][c-1].visited) neighbors.add(3);
            if (neighbors.isEmpty()) {
                stack.pop();
            } else {
                int dir = neighbors.get(rng.nextInt(neighbors.size()));
                int nr = r + dirs[dir][0], nc = c + dirs[dir][1];
                if (dir == 0) { grid[r][c].wallTop = false; grid[nr][nc].wallBottom = false; }
                if (dir == 1) { grid[r][c].wallRight = false; grid[nr][nc].wallLeft = false; }
                if (dir == 2) { grid[r][c].wallBottom = false; grid[nr][nc].wallTop = false; }
                if (dir == 3) { grid[r][c].wallLeft = false; grid[nr][nc].wallRight = false; }
                grid[nr][nc].visited = true;
                stack.push(new int[]{nr, nc});
            }
        }
        // Force open a horizontal corridor band through middle to help entry (left) to exit (right)
        int midR = rows / 2;
        for (int c = 0; c < cols; c++) {
            grid[midR][c].wallLeft = false;
            grid[midR][c].wallRight = false;
            if (midR > 0) grid[midR][c].wallTop = false;
            if (midR < rows - 1) grid[midR][c].wallBottom = false;
        }
        // Also ensure leftmost and rightmost column middle are passable vertically into band
        if (midR > 0) { grid[midR-1][0].wallBottom = false; grid[midR][0].wallTop = false; }
        if (midR < rows-1) { grid[midR+1][0].wallTop = false; grid[midR][0].wallBottom = false; }
        if (midR > 0) { grid[midR-1][cols-1].wallBottom = false; grid[midR][cols-1].wallTop = false; }
        if (midR < rows-1) { grid[midR+1][cols-1].wallTop = false; grid[midR][cols-1].wallBottom = false; }

        int wallThick = Math.max(6, cellSize / 6);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int cx = area.x + c * cellSize;
                int cy = area.y + r * cellSize;
                Cell cell = grid[r][c];
                if (cell.wallTop) walls.add(new Rect(cx, cy, cellSize, wallThick));
                if (cell.wallLeft) walls.add(new Rect(cx, cy, wallThick, cellSize));
                if (c == cols - 1 && cell.wallRight) walls.add(new Rect(cx + cellSize - wallThick, cy, wallThick, cellSize));
                if (r == rows - 1 && cell.wallBottom) walls.add(new Rect(cx, cy + cellSize - wallThick, cellSize, wallThick));
            }
        }
        return walls;
    }
}
