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
        int sr = rng.nextInt(rows), sc = rng.nextInt(cols);
        grid[sr][sc].visited = true;
        stack.push(new int[]{sr, sc});
        int[][] dirs = {{-1,0,0},{0,1,1},{1,0,2},{0,-1,3}}; // dr,dc,dir index
        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            int r = cur[0], c = cur[1];
            List<Integer> neighbors = new ArrayList<>();
            // top
            if (r > 0 && !grid[r-1][c].visited) neighbors.add(0);
            // right
            if (c < cols-1 && !grid[r][c+1].visited) neighbors.add(1);
            // bottom
            if (r < rows-1 && !grid[r+1][c].visited) neighbors.add(2);
            // left
            if (c > 0 && !grid[r][c-1].visited) neighbors.add(3);
            if (neighbors.isEmpty()) {
                stack.pop();
            } else {
                int dir = neighbors.get(rng.nextInt(neighbors.size()));
                int nr = r + dirs[dir][0], nc = c + dirs[dir][1];
                // remove walls between (r,c) and (nr,nc)
                if (dir == 0) { grid[r][c].wallTop = false; grid[nr][nc].wallBottom = false; }
                if (dir == 1) { grid[r][c].wallRight = false; grid[nr][nc].wallLeft = false; }
                if (dir == 2) { grid[r][c].wallBottom = false; grid[nr][nc].wallTop = false; }
                if (dir == 3) { grid[r][c].wallLeft = false; grid[nr][nc].wallRight = false; }
                grid[nr][nc].visited = true;
                stack.push(new int[]{nr, nc});
            }
        }

        // Convert walls to rects. For each cell, add top wall if exists (except top row edge handling), right wall etc.
        int wallThick = Math.max(6, cellSize / 6);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int cx = area.x + c * cellSize;
                int cy = area.y + r * cellSize;
                Cell cell = grid[r][c];
                // top wall
                if (cell.wallTop) {
                    walls.add(new Rect(cx, cy, cellSize, wallThick));
                }
                // left wall
                if (cell.wallLeft) {
                    walls.add(new Rect(cx, cy, wallThick, cellSize));
                }
                // right wall (only for last column to avoid duplicates)
                if (c == cols - 1 && cell.wallRight) {
                    walls.add(new Rect(cx + cellSize - wallThick, cy, wallThick, cellSize));
                }
                // bottom wall (only for last row)
                if (r == rows - 1 && cell.wallBottom) {
                    walls.add(new Rect(cx, cy + cellSize - wallThick, cellSize, wallThick));
                }
            }
        }
        // Carve entry/exit openings roughly: open left side of leftmost column middle and right side of rightmost column middle
        // This helps connectivity to room doors; we’ll also place goal button in a passage cell
        return walls;
    }
}
