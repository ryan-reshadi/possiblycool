package Levels.Generation;

public final class RoomGraphEdge {
    public final int from;
    public final int to;
    public LockSpec lock; // null = open

    public RoomGraphEdge(int from, int to) {
        this.from = from;
        this.to = to;
        this.lock = null;
    }

    public RoomGraphEdge(int from, int to, LockSpec lock) {
        this.from = from;
        this.to = to;
        this.lock = lock;
    }
}
