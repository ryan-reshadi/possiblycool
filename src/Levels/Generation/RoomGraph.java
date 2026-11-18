package Levels.Generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RoomGraph {
    public final List<RoomGraphNode> nodes = new ArrayList<>();
    public final List<RoomGraphEdge> edges = new ArrayList<>();

    public RoomGraphNode addNode(RoomRole role) {
        RoomGraphNode n = new RoomGraphNode(nodes.size(), role);
        nodes.add(n);
        return n;
    }

    public void addEdge(int from, int to) {
        edges.add(new RoomGraphEdge(from, to));
    }

    public void addEdge(int from, int to, LockSpec lock) {
        edges.add(new RoomGraphEdge(from, to, lock));
    }
}
