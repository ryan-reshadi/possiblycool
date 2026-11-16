package Levels.Generation;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Generates navigable multi-room layouts from explicit room connections.
 * Corridors and doorways are derived from declared paths (not inferred heuristics) and
 * validated for reachability, with a safe fallback to guarantee passable levels.
 */
public final class LayoutGenerator {
    private static final int WALL = 20;
    private static final int GAP = 120; // doorway/corridor width, wide enough for AI/player
    private static final int MARGIN = 180;
    private static final int CELL = 20;

    private LayoutGenerator() {}

    private enum Side { TOP, BOTTOM, LEFT, RIGHT }

    private static final class Door {
        final Side side;
        final int offset; // distance from room's left/top along the wall
        final int len;
        Door(Side side, int offset, int len) { this.side = side; this.offset = offset; this.len = len; }
    }

    /** Explicit connection/path between two room doorway anchors. */
    private static final class Connection {
        final int roomA;
        final Side sideA;
        final int offsetA;
        final int roomB;
        final Side sideB;
        final int offsetB;
        final List<Rect> segments = new ArrayList<>();
        Connection(int roomA, Side sideA, int offsetA, int roomB, Side sideB, int offsetB) {
            this.roomA = roomA; this.sideA = sideA; this.offsetA = offsetA;
            this.roomB = roomB; this.sideB = sideB; this.offsetB = offsetB;
        }
    }

    private static final class Layout {
        final LevelBlueprint bp = new LevelBlueprint();
        final List<Rect> rooms = new ArrayList<>();
        final List<List<Door>> doors = new ArrayList<>();
        final List<Rect> corridors = new ArrayList<>();
        void addRoom(Rect r) { rooms.add(r); bp.addRoom(r); doors.add(new ArrayList<>()); }
        void addDoor(int roomIndex, Door door) { doors.get(roomIndex).add(door); }
    }

    public static LevelBlueprint generateCombat(GenerationContext ctx) {
        for (int attempt = 0; attempt < 5; attempt++) {
            LevelBlueprint bp = tryGenerateCombat(ctx, attempt);
            if (isValid(bp, false)) return bp;
        }
        return fallbackCombat(ctx);
    }

    public static LevelBlueprint generateBoss(GenerationContext ctx) {
        for (int attempt = 0; attempt < 5; attempt++) {
            LevelBlueprint bp = tryGenerateBoss(ctx, attempt);
            if (isValid(bp, true)) return bp;
        }
        return fallbackBoss(ctx);
    }

    private static LevelBlueprint tryGenerateCombat(GenerationContext ctx, int attempt) {
        Random r = new Random(ctx.random.nextLong() ^ (attempt * 0x9E3779B97F4A7C15L));
        Layout layout = new Layout();
        int roomCount = 4 + r.nextInt(2); // 4-5 main rooms
        int roomW = 520;
        int roomH = 420;
        int spacing = 300;
        layout.bp.width = MARGIN * 2 + roomCount * roomW + (roomCount - 1) * spacing;
        layout.bp.height = 1600;

        int baseY = 360 + r.nextInt(160);
        for (int i = 0; i < roomCount; i++) {
            int rx = MARGIN + i * (roomW + spacing);
            int ry = baseY + (i % 2 == 0 ? 0 : (r.nextInt(120) - 60));
            ry = clamp(ry, 180, layout.bp.height - roomH - 180);
            layout.addRoom(new Rect(rx, ry, roomW, roomH));
        }

        List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < roomCount - 1; i++) {
            connections.add(connectRooms(layout, i, i + 1));
        }

        // Optional side reward room off room 1 or 2 (only if non-overlapping placement exists)
        int sideIndex = -1;
        if (roomCount >= 4) {
            int host = 1 + r.nextInt(Math.min(2, roomCount - 2));
            sideIndex = tryPlaceSideRoom(layout, connections, host, r);
        }

        for (Connection c : connections) layout.corridors.addAll(c.segments);

        buildWalls(layout);

        Rect start = layout.rooms.get(0);
        layout.bp.playerSpawn = new Point(start.centerX() - 20, start.centerY() - 20);

        int guardRoomIndex = roomCount - 2;
        Rect guardRoom = layout.rooms.get(guardRoomIndex);
        // Gate on the doorway from guard room toward the final room (main path)
        Door guardDoorway = findDoorToward(layout, guardRoomIndex, roomCount - 1, Side.RIGHT);
        LevelBlueprint.GateSpec gate = null;
        if (guardDoorway != null) {
            Rect gateDoor = doorBarrierRect(guardRoom, guardDoorway);
            gate = new LevelBlueprint.GateSpec(gateDoor, guardRoomIndex);
            layout.bp.gates.add(gate);
        }

        // Encounters in main rooms (skip start, side is reward-only)
        for (int i = 1; i < roomCount; i++) {
            Rect rm = layout.rooms.get(i);
            boolean isGuardRoom = (i == guardRoomIndex);
            boolean isFinal = (i == roomCount - 1);
            int count = isGuardRoom ? Math.min(4, 2 + ctx.difficulty() / 3)
                    : (isFinal ? Math.min(5, 3 + ctx.difficulty() / 3) : Math.min(4, 1 + i + ctx.difficulty() / 3));
            count = clamp(count, 1, 6);
            placeEnemies(layout.bp, ctx, r, rm, i, count, isGuardRoom, isFinal, gate);
        }

        // Chest
        if (sideIndex >= 0) {
            Rect side = layout.rooms.get(sideIndex);
            layout.bp.chests.add(new LevelBlueprint.ChestSpec(side.centerX() - 20, side.centerY() - 20, 2 + r.nextInt(2), ctx.difficulty()));
        } else {
            Rect r2 = layout.rooms.get(Math.min(2, roomCount - 2));
            layout.bp.chests.add(new LevelBlueprint.ChestSpec(r2.centerX() + 40, r2.top() + 40, 2, ctx.difficulty()));
        }

        // Checkpoint in room 1
        Rect safe = layout.rooms.get(1);
        layout.bp.checkpoints.add(new Point(safe.left() + 50, safe.centerY()));

        return layout.bp;
    }

    private static int tryPlaceSideRoom(Layout layout, List<Connection> connections, int host, Random r) {
        Rect hostRect = layout.rooms.get(host);
        int sw = 340, sh = 300;
        boolean preferUp = hostRect.centerY() > layout.bp.height / 2;
        boolean[] ups = r.nextBoolean() ? new boolean[]{preferUp, !preferUp} : new boolean[]{!preferUp, preferUp};
        int[] xJitter = {0, 60, -60, 120, -120};
        for (boolean up : ups) {
            for (int j : xJitter) {
                int sx = clamp(hostRect.centerX() - sw / 2 + j, MARGIN, layout.bp.width - sw - MARGIN);
                int sy = up ? hostRect.top() - sh - 260 : hostRect.bottom() + 260;
                sy = clamp(sy, 120, layout.bp.height - sh - 120);
                Rect candidate = new Rect(sx, sy, sw, sh);
                if (overlapsAny(candidate, layout.rooms, 40)) continue;
                layout.addRoom(candidate);
                int sideIndex = layout.rooms.size() - 1;
                connections.add(connectRooms(layout, host, sideIndex));
                return sideIndex;
            }
        }
        return -1;
    }

    private static boolean overlapsAny(Rect candidate, List<Rect> rooms, int margin) {
        Rect expanded = new Rect(candidate.x - margin, candidate.y - margin, candidate.w + margin * 2, candidate.h + margin * 2);
        for (Rect r : rooms) if (expanded.intersects(r)) return true;
        return false;
    }

    private static Rect doorBarrierRect(Rect room, Door d) {
        switch (d.side) {
            case RIGHT: return new Rect(room.right() - WALL, room.y + d.offset, WALL, d.len);
            case LEFT: return new Rect(room.x, room.y + d.offset, WALL, d.len);
            case BOTTOM: return new Rect(room.x + d.offset, room.bottom() - WALL, d.len, WALL);
            case TOP: return new Rect(room.x + d.offset, room.y, d.len, WALL);
            default: return new Rect(room.right() - WALL, room.y + d.offset, WALL, d.len);
        }
    }

    private static LevelBlueprint tryGenerateBoss(GenerationContext ctx, int attempt) {
        Random r = new Random(ctx.random.nextLong() ^ (attempt * 0xA24BAED4963EE407L));
        Layout layout = new Layout();
        layout.bp.width = 2800;
        layout.bp.height = 1400;
        Rect start = new Rect(220, 450, 420, 500);
        Rect antechamber = new Rect(940, 350, 520, 700);
        Rect arena = new Rect(1700, 250, 900, 900);
        layout.addRoom(start);      // 0
        layout.addRoom(antechamber);// 1
        layout.addRoom(arena);      // 2

        List<Connection> connections = new ArrayList<>();
        connections.add(connectRooms(layout, 0, 1));
        connections.add(connectRooms(layout, 1, 2));
        for (Connection c : connections) layout.corridors.addAll(c.segments);
        buildWalls(layout);

        layout.bp.playerSpawn = new Point(start.centerX() - 20, start.centerY() - 20);
        layout.bp.checkpoints.add(new Point(antechamber.centerX(), antechamber.bottom() - 60));

        placeEnemies(layout.bp, ctx, r, antechamber, 1, Math.min(3, 1 + ctx.stage / 3), false, false, null);
        placeEnemies(layout.bp, ctx, r, arena, 2, Math.min(2, 1 + ctx.stage / 4), false, false, null);

        int bossHealth = 260 + Math.max(0, ctx.stageCount - 8) * 30 + ctx.stage * 10;
        layout.bp.boss = new LevelBlueprint.BossSpec(arena.centerX() - 20, arena.centerY() - 20, bossHealth);
        return layout.bp;
    }

    private static LevelBlueprint fallbackCombat(GenerationContext ctx) {
        Layout layout = new Layout();
        int count = 4;
        int rw = 520, rh = 420, spacing = 260;
        layout.bp.width = MARGIN * 2 + count * rw + (count - 1) * spacing;
        layout.bp.height = 1000;
        for (int i = 0; i < count; i++) layout.addRoom(new Rect(MARGIN + i * (rw + spacing), 300, rw, rh));
        List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < count - 1; i++) connections.add(connectRooms(layout, i, i + 1));
        for (Connection c : connections) layout.corridors.addAll(c.segments);
        buildWalls(layout);
        Rect start = layout.rooms.get(0);
        layout.bp.playerSpawn = new Point(start.centerX() - 20, start.centerY() - 20);
        Door d = findDoorToward(layout, count - 2, count - 1, Side.RIGHT);
        LevelBlueprint.GateSpec gate = null;
        if (d != null) {
            Rect gr = layout.rooms.get(count - 2);
            gate = new LevelBlueprint.GateSpec(new Rect(gr.right() - WALL, gr.y + d.offset, WALL, d.len), count - 2);
            layout.bp.gates.add(gate);
        }
        Random r = new Random(ctx.seed ^ 0xF00DL);
        for (int i = 1; i < count; i++) {
            boolean guard = i == count - 2, fin = i == count - 1;
            int n = guard ? 3 : fin ? 4 : 3;
            placeEnemies(layout.bp, ctx, r, layout.rooms.get(i), i, n, guard, fin, gate);
        }
        layout.bp.chests.add(new LevelBlueprint.ChestSpec(layout.rooms.get(1).centerX(), layout.rooms.get(1).centerY() - 40, 2, ctx.difficulty()));
        layout.bp.checkpoints.add(new Point(layout.rooms.get(1).left() + 50, layout.rooms.get(1).centerY()));
        return layout.bp;
    }

    private static LevelBlueprint fallbackBoss(GenerationContext ctx) {
        Layout layout = new Layout();
        layout.bp.width = 2400; layout.bp.height = 1200;
        layout.addRoom(new Rect(220, 380, 420, 440));   // 0
        layout.addRoom(new Rect(900, 300, 520, 600));   // 1
        layout.addRoom(new Rect(1600, 260, 700, 680));  // 2
        List<Connection> connections = new ArrayList<>();
        connections.add(connectRooms(layout, 0, 1));
        connections.add(connectRooms(layout, 1, 2));
        for (Connection c : connections) layout.corridors.addAll(c.segments);
        buildWalls(layout);
        layout.bp.playerSpawn = new Point(layout.rooms.get(0).centerX() - 20, layout.rooms.get(0).centerY() - 20);
        layout.bp.checkpoints.add(new Point(layout.rooms.get(1).centerX(), layout.rooms.get(1).bottom() - 60));
        Random r = new Random(ctx.seed ^ 0xB055L);
        placeEnemies(layout.bp, ctx, r, layout.rooms.get(1), 1, 2, false, false, null);
        int bossHealth = 260 + Math.max(0, ctx.stageCount - 8) * 30 + ctx.stage * 10;
        layout.bp.boss = new LevelBlueprint.BossSpec(layout.rooms.get(2).centerX() - 20, layout.rooms.get(2).centerY() - 20, bossHealth);
        return layout.bp;
    }

    /** Create an explicit, walled connection between two rooms with declared doorways. */
    private static Connection connectRooms(Layout layout, int aIdx, int bIdx) {
        Rect a = layout.rooms.get(aIdx);
        Rect b = layout.rooms.get(bIdx);
        int dx = b.centerX() - a.centerX();
        int dy = b.centerY() - a.centerY();
        Side sideA, sideB;
        if (Math.abs(dx) >= Math.abs(dy)) {
            sideA = dx >= 0 ? Side.RIGHT : Side.LEFT;
            sideB = dx >= 0 ? Side.LEFT : Side.RIGHT;
        } else {
            sideA = dy >= 0 ? Side.BOTTOM : Side.TOP;
            sideB = dy >= 0 ? Side.TOP : Side.BOTTOM;
        }
        int offA = doorOffset(a, sideA, b);
        int offB = doorOffset(b, sideB, a);
        layout.addDoor(aIdx, new Door(sideA, offA, GAP));
        layout.addDoor(bIdx, new Door(sideB, offB, GAP));
        Connection c = new Connection(aIdx, sideA, offA, bIdx, sideB, offB);
        Point pa = doorPoint(a, sideA, offA);
        Point pb = doorPoint(b, sideB, offB);
        buildPathSegments(c, pa, sideA, pb, sideB);
        return c;
    }

    private static int doorOffset(Rect room, Side side, Rect toward) {
        if (side == Side.LEFT || side == Side.RIGHT) {
            int target = toward.centerY() - GAP / 2 - room.y;
            return clamp(target, WALL, room.h - WALL - GAP);
        } else {
            int target = toward.centerX() - GAP / 2 - room.x;
            return clamp(target, WALL, room.w - WALL - GAP);
        }
    }

    /** Point just outside the room at the doorway's leading edge (start of corridor). */
    private static Point doorPoint(Rect room, Side side, int offset) {
        switch (side) {
            case RIGHT: return new Point(room.right(), room.y + offset);
            case LEFT: return new Point(room.x - GAP, room.y + offset); // corridor rect will extend leftward
            case BOTTOM: return new Point(room.x + offset, room.bottom());
            case TOP: return new Point(room.x + offset, room.y - GAP);
            default: throw new IllegalArgumentException();
        }
    }

    private static void buildPathSegments(Connection c, Point pa, Side sideA, Point pb, Side sideB) {
        // pa/pb are the corridor-rect origins adjacent to each room opening.
        if ((sideA == Side.RIGHT && sideB == Side.LEFT) || (sideA == Side.LEFT && sideB == Side.RIGHT)) {
            int y1 = pa.y, y2 = pb.y;
            int x1 = sideA == Side.RIGHT ? pa.x : pb.x; // left end (outside right-side room)
            int x2 = sideA == Side.RIGHT ? pb.x : pa.x; // right end origin near left-side room
            int corridorRight = x2 + GAP; // pb is origin at left side of room B (x = roomB.x - GAP)
            int midX = x1 + (corridorRight - x1) / 2 - GAP / 2;
            midX = Math.max(x1, Math.min(corridorRight - GAP, midX));
            c.segments.add(rectFrom(x1, y1, midX + GAP, y1 + GAP));
            int ry = Math.min(y1, y2);
            c.segments.add(rectFrom(midX, ry, midX + GAP, ry + Math.abs(y2 - y1) + GAP));
            c.segments.add(rectFrom(midX, y2, corridorRight, y2 + GAP));
        } else if ((sideA == Side.BOTTOM && sideB == Side.TOP) || (sideA == Side.TOP && sideB == Side.BOTTOM)) {
            int x1 = pa.x, x2 = pb.x;
            int y1 = sideA == Side.BOTTOM ? pa.y : pb.y; // top end
            int y2 = sideA == Side.BOTTOM ? pb.y : pa.y; // bottom end origin near top-side room
            int corridorBottom = y2 + GAP;
            int midY = y1 + (corridorBottom - y1) / 2 - GAP / 2;
            midY = Math.max(y1, Math.min(corridorBottom - GAP, midY));
            c.segments.add(rectFrom(x1, y1, x1 + GAP, midY + GAP));
            int rx = Math.min(x1, x2);
            c.segments.add(rectFrom(rx, midY, rx + Math.abs(x2 - x1) + GAP, midY + GAP));
            c.segments.add(rectFrom(x2, midY, x2 + GAP, corridorBottom));
        } else {
            // Fallback L-shape for uncommon side pairings
            c.segments.add(rectFrom(Math.min(pa.x, pb.x), pa.y, Math.max(pa.x, pb.x) + GAP, pa.y + GAP));
            c.segments.add(rectFrom(pb.x, Math.min(pa.y, pb.y), pb.x + GAP, Math.max(pa.y, pb.y) + GAP));
        }
        c.segments.removeIf(s -> s.w <= 0 || s.h <= 0);
    }

    private static Rect rectFrom(int x1, int y1, int x2, int y2) {
        int rx = Math.min(x1, x2), ry = Math.min(y1, y2);
        return new Rect(rx, ry, Math.max(1, x2 - rx), Math.max(1, y2 - ry));
    }

    private static Door findDoorToward(Layout layout, int fromRoom, int toRoom, Side preferred) {
        // find door on fromRoom whose side points roughly toward toRoom
        Rect from = layout.rooms.get(fromRoom);
        Rect to = layout.rooms.get(toRoom);
        Side want;
        int dx = to.centerX() - from.centerX();
        int dy = to.centerY() - from.centerY();
        want = Math.abs(dx) >= Math.abs(dy) ? (dx >= 0 ? Side.RIGHT : Side.LEFT) : (dy >= 0 ? Side.BOTTOM : Side.TOP);
        for (Door d : layout.doors.get(fromRoom)) if (d.side == want) return d;
        for (Door d : layout.doors.get(fromRoom)) if (d.side == preferred) return d;
        return layout.doors.get(fromRoom).isEmpty() ? null : layout.doors.get(fromRoom).get(0);
    }

    private static void buildWalls(Layout layout) {
        // Room perimeter walls carved at explicit doorways
        for (int i = 0; i < layout.rooms.size(); i++) {
            addRoomWalls(layout.bp, layout.rooms.get(i), layout.doors.get(i));
        }
        // Corridor edge walls, carved at junctions with other corridors and at room doorway mouths
        List<Rect> mouths = mouthRects(layout);
        for (Rect c : layout.corridors) {
            addCorridorWalls(layout.bp, c, layout.corridors, mouths);
        }
    }

    private static List<Rect> mouthRects(Layout layout) {
        List<Rect> mouths = new ArrayList<>();
        for (int i = 0; i < layout.rooms.size(); i++) {
            Rect room = layout.rooms.get(i);
            for (Door d : layout.doors.get(i)) {
                mouths.add(mouthRect(room, d));
            }
        }
        return mouths;
    }

    /** Area spanning the doorway/opening where corridor meets room interior boundary. */
    private static Rect mouthRect(Rect room, Door d) {
        switch (d.side) {
            case RIGHT: return new Rect(room.right() - WALL, room.y + d.offset, WALL + GAP, d.len);
            case LEFT: return new Rect(room.x - GAP, room.y + d.offset, GAP + WALL, d.len);
            case BOTTOM: return new Rect(room.x + d.offset, room.bottom() - WALL, d.len, WALL + GAP);
            case TOP: return new Rect(room.x + d.offset, room.y - GAP, d.len, GAP + WALL);
            default: throw new IllegalArgumentException();
        }
    }

    private static void addRoomWalls(LevelBlueprint bp, Rect room, List<Door> doors) {
        addWallEdge(bp, room.x, room.y, room.w, WALL, true, doors, Side.TOP, room);
        addWallEdge(bp, room.x, room.bottom() - WALL, room.w, WALL, true, doors, Side.BOTTOM, room);
        addWallEdge(bp, room.x, room.y, WALL, room.h, false, doors, Side.LEFT, room);
        addWallEdge(bp, room.right() - WALL, room.y, WALL, room.h, false, doors, Side.RIGHT, room);
    }

    private static void addWallEdge(LevelBlueprint bp, int x, int y, int w, int h, boolean horizontal,
                                    List<Door> doors, Side side, Rect room) {
        List<int[]> gaps = new ArrayList<>();
        for (Door d : doors) {
            if (d.side != side) continue;
            int start = clamp(d.offset, WALL, (horizontal ? room.w : room.h) - WALL - d.len);
            gaps.add(new int[]{start, start + d.len});
        }
        gaps.sort((a, b) -> Integer.compare(a[0], b[0]));
        int total = horizontal ? w : h;
        int cursor = 0;
        for (int[] g : mergeGaps(gaps, total)) {
            if (g[0] > cursor) {
                if (horizontal) bp.walls.add(new Rect(x + cursor, y, g[0] - cursor, h));
                else bp.walls.add(new Rect(x, y + cursor, w, g[0] - cursor));
            }
            cursor = Math.max(cursor, g[1]);
        }
        if (cursor < total) {
            if (horizontal) bp.walls.add(new Rect(x + cursor, y, total - cursor, h));
            else bp.walls.add(new Rect(x, y + cursor, w, total - cursor));
        }
    }

    private static void addCorridorWalls(LevelBlueprint bp, Rect c, List<Rect> allCorridors, List<Rect> mouths) {
        // Wall all four edges of each corridor segment; carve openings wherever the segment
        // adjoins another corridor segment or a declared room doorway mouth. This yields
        // a continuous bounded tube regardless of segment orientation/size.
        addCorridorEdge(bp, c.x - WALL, c.y - WALL, c.w + 2 * WALL, WALL, true, c, allCorridors, mouths); // top
        addCorridorEdge(bp, c.x - WALL, c.bottom(), c.w + 2 * WALL, WALL, true, c, allCorridors, mouths); // bottom
        addCorridorEdge(bp, c.x - WALL, c.y, WALL, c.h, false, c, allCorridors, mouths); // left
        addCorridorEdge(bp, c.right(), c.y, WALL, c.h, false, c, allCorridors, mouths); // right
    }

    private static void addCorridorEdge(LevelBlueprint bp, int x, int y, int w, int h, boolean horizontal,
                                        Rect owner, List<Rect> allCorridors, List<Rect> mouths) {
        List<int[]> open = new ArrayList<>();
        // Open where this edge runs alongside another corridor segment or passes a junction
        for (Rect other : allCorridors) {
            if (other == owner) continue;
            collectAdjacency(open, x, y, w, h, horizontal, other);
        }
        // Open where corridor meets a room doorway mouth
        for (Rect mouth : mouths) {
            collectAdjacency(open, x, y, w, h, horizontal, mouth);
        }
        int total = horizontal ? w : h;
        List<int[]> merged = mergeGaps(open, total);
        if (merged.isEmpty()) {
            bp.walls.add(new Rect(x, y, w, h));
            return;
        }
        int cursor = 0;
        for (int[] g : merged) {
            if (g[0] > cursor) {
                if (horizontal) bp.walls.add(new Rect(x + cursor, y, g[0] - cursor, h));
                else bp.walls.add(new Rect(x, y + cursor, w, g[0] - cursor));
            }
            cursor = Math.max(cursor, g[1]);
        }
        if (cursor < total) {
            if (horizontal) bp.walls.add(new Rect(x + cursor, y, total - cursor, h));
            else bp.walls.add(new Rect(x, y + cursor, w, total - cursor));
        }
    }

    private static void collectAdjacency(List<int[]> open, int x, int y, int w, int h, boolean horizontal, Rect area) {
        // Carve this wall edge wherever an adjacent/open area (other corridor or room mouth)
        // overlaps along the edge span, using tolerance for exact boundary alignment.
        if (horizontal) {
            int ox1 = Math.max(x, area.x - 1), ox2 = Math.min(x + w, area.right() + 1);
            boolean adjacentY = area.y <= y + h + 1 && area.bottom() >= y - 1;
            if (adjacentY && ox2 > ox1) open.add(new int[]{ox1 - x, ox2 - x});
        } else {
            int oy1 = Math.max(y, area.y - 1), oy2 = Math.min(y + h, area.bottom() + 1);
            boolean adjacentX = area.x <= x + w + 1 && area.right() >= x - 1;
            if (adjacentX && oy2 > oy1) open.add(new int[]{oy1 - y, oy2 - y});
        }
    }

    private static List<int[]> mergeGaps(List<int[]> gaps, int total) {
        if (gaps.isEmpty()) return java.util.Collections.emptyList();
        gaps.sort((a, b) -> Integer.compare(a[0], b[0]));
        List<int[]> merged = new ArrayList<>();
        for (int[] b : gaps) {
            int s = clamp(b[0], 0, total);
            int e = clamp(b[1], 0, total);
            if (e <= s) continue;
            if (merged.isEmpty() || s > merged.get(merged.size() - 1)[1]) merged.add(new int[]{s, e});
            else merged.get(merged.size() - 1)[1] = Math.max(merged.get(merged.size() - 1)[1], e);
        }
        return merged;
    }

    // ---- population helpers ----

    private static void placeEnemies(LevelBlueprint bp, GenerationContext ctx, Random r, Rect room, int roomIndex,
                                     int count, boolean guards, boolean isFinal, LevelBlueprint.GateSpec gate) {
        Rect inner = room.inset(50);
        for (int i = 0; i < count; i++) {
            int ex = inner.x + r.nextInt(Math.max(1, inner.w - 40));
            int ey = inner.y + r.nextInt(Math.max(1, inner.h - 40));
            LevelBlueprint.EnemyKind kind = pickEnemyKind(ctx, r, guards, isFinal);
            bp.enemies.add(new LevelBlueprint.EnemySpawn(kind, ex, ey, roomIndex));
            if (guards && gate != null) gate.guardEnemyIndices.add(bp.enemies.size() - 1);
        }
    }

    private static LevelBlueprint.EnemyKind pickEnemyKind(GenerationContext ctx, Random r, boolean guards, boolean isFinal) {
        int roll = r.nextInt(100);
        if (guards) {
            if (roll < 45) return LevelBlueprint.EnemyKind.BERSERKER;
            if (roll < 75) return LevelBlueprint.EnemyKind.TANK;
            return LevelBlueprint.EnemyKind.MELEE;
        }
        if (isFinal) {
            if (roll < 30) return LevelBlueprint.EnemyKind.RANGED;
            if (roll < 55) return LevelBlueprint.EnemyKind.BERSERKER;
            if (roll < 75) return LevelBlueprint.EnemyKind.TANK;
            return LevelBlueprint.EnemyKind.MELEE;
        }
        if (ctx.difficulty() <= 2) {
            return roll < 70 ? LevelBlueprint.EnemyKind.MELEE : LevelBlueprint.EnemyKind.RANGED;
        }
        if (roll < 35) return LevelBlueprint.EnemyKind.MELEE;
        if (roll < 55) return LevelBlueprint.EnemyKind.RANGED;
        if (roll < 75) return LevelBlueprint.EnemyKind.BERSERKER;
        return LevelBlueprint.EnemyKind.TANK;
    }

    // ---- validation ----

    private static boolean isValid(LevelBlueprint bp, boolean boss) {
        if (bp.rooms.isEmpty()) return false;
        int cols = Math.max(1, (bp.width + CELL - 1) / CELL);
        int rows = Math.max(1, (bp.height + CELL - 1) / CELL);
        boolean[][] blocked = new boolean[rows][cols];
        for (Rect w : bp.walls) {
            int x0 = clamp(w.x / CELL, 0, cols - 1), x1 = clamp((w.right() - 1) / CELL, 0, cols - 1);
            int y0 = clamp(w.y / CELL, 0, rows - 1), y1 = clamp((w.bottom() - 1) / CELL, 0, rows - 1);
            for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++) blocked[y][x] = true;
        }
        // Gates are runtime blockers until cleared; treat as passable for layout solvability of the intended route.
        int sx = clamp(bp.playerSpawn.x / CELL, 0, cols - 1);
        int sy = clamp(bp.playerSpawn.y / CELL, 0, rows - 1);
        boolean[][] vis = new boolean[rows][cols];
        ArrayDeque<int[]> q = new ArrayDeque<>();
        if (!blocked[sy][sx]) { vis[sy][sx] = true; q.add(new int[]{sx, sy}); }
        int[] dx = {1, -1, 0, 0}, dy = {0, 0, 1, -1};
        while (!q.isEmpty()) {
            int[] c = q.poll();
            for (int d = 0; d < 4; d++) {
                int nx = c[0] + dx[d], ny = c[1] + dy[d];
                if (nx < 0 || ny < 0 || nx >= cols || ny >= rows || vis[ny][nx] || blocked[ny][nx]) continue;
                vis[ny][nx] = true; q.add(new int[]{nx, ny});
            }
        }
        // No room overlaps
        for (int i = 0; i < bp.rooms.size(); i++) {
            for (int j = i + 1; j < bp.rooms.size(); j++) {
                if (bp.rooms.get(i).intersects(bp.rooms.get(j))) return false;
            }
        }
        if (!vis[sy][sx]) return false;
        // Require all room centers reachable from spawn (ensures connected corridors/doors)
        for (Rect room : bp.rooms) {
            int tx = clamp(room.centerX() / CELL, 0, cols - 1);
            int ty = clamp(room.centerY() / CELL, 0, rows - 1);
            if (!reachableNear(vis, tx, ty, 3)) return false;
        }
        if (boss && bp.boss != null) {
            int tx = clamp(bp.boss.x / CELL, 0, cols - 1), ty = clamp(bp.boss.y / CELL, 0, rows - 1);
            if (!reachableNear(vis, tx, ty, 3)) return false;
        }
        return true;
    }

    private static boolean reachableNear(boolean[][] vis, int tx, int ty, int radius) {
        for (int oy = -radius; oy <= radius; oy++) for (int ox = -radius; ox <= radius; ox++) {
            int ny = ty + oy, nx = tx + ox;
            if (ny >= 0 && ny < vis.length && nx >= 0 && nx < vis[0].length && vis[ny][nx]) return true;
        }
        return false;
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
}
