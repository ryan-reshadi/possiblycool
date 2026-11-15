package GameRun;

import Objects.PlayerClasses.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Generated, persisted branching route. Nodes are committed at room/shop boundaries. */
public final class RunRoute {
    public static final int DEFAULT_STAGES = 8;
    public enum Type { TUTORIAL, ROOM, SHOP, BOSS }

    public static final class Node {
        public final String id;
        public final String title;
        public final int stage;
        public final Type type;
        public final List<String> next = new ArrayList<>();
        public final List<Map<String, Object>> shopStock = new ArrayList<>();
        public final Set<String> purchased = new LinkedHashSet<>();

        Node(String id, String title, int stage, Type type) {
            this.id = id; this.title = title; this.stage = stage; this.type = type;
        }
    }

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final int stageCount;
    private final long seed;
    private int currentStage;
    private String previousNodeId;
    private String pendingNodeId;

    private RunRoute(int stageCount, long seed) {
        this.stageCount = Math.max(1, stageCount);
        this.seed = seed;
    }

    public static RunRoute generate(int stageCount, long seed) {
        RunRoute route = new RunRoute(stageCount, seed);
        Random random = new Random(seed);
        Node tutorial = new Node("tutorial", "Level 1 - Tutorial", 0, Type.TUTORIAL);
        route.nodes.put(tutorial.id, tutorial);
        List<Node> previous = List.of(tutorial);
        int shopStageA = route.stageCount >= 6 ? 3 : Math.max(1, route.stageCount / 3);
        int shopStageB = route.stageCount >= 6 ? 6
                : Math.min(route.stageCount, Math.max(shopStageA + 2, route.stageCount - 1));
        for (int stage = 1; stage <= route.stageCount; stage++) {
            int width = 2 + random.nextInt(2);
            int shopIndex = (stage == shopStageA || stage == shopStageB) ? random.nextInt(width) : -1;
            List<Node> layer = new ArrayList<>();
            for (int i = 0; i < width; i++) {
                Type type = i == shopIndex ? Type.SHOP : Type.ROOM;
                Node node = new Node("stage-" + stage + "-" + i,
                        type == Type.SHOP ? "Shop" : "Room " + stage, stage, type);
                route.nodes.put(node.id, node);
                layer.add(node);
            }
            route.connect(previous, layer);
            previous = layer;
        }
        Node boss = new Node("final-boss", "Final Boss", route.stageCount + 1, Type.BOSS);
        route.nodes.put(boss.id, boss);
        route.connect(previous, List.of(boss));
        return route;
    }

    private void connect(List<Node> from, List<Node> to) {
        if (from.size() == 1) {
            for (Node destination : to) from.get(0).next.add(destination.id);
            return;
        }
        for (int i = 0; i < from.size(); i++) {
            Node source = from.get(i);
            Node first = to.get((int) ((long) i * to.size() / from.size()));
            source.next.add(first.id);
            Node second = to.get((int) ((long) (i + 1) * to.size() / from.size()) % to.size());
            if (!source.next.contains(second.id)) source.next.add(second.id);
        }
    }

    public Node getNode(String id) { return nodes.get(id); }
    public List<Node> getNodes() { return Collections.unmodifiableList(new ArrayList<>(nodes.values())); }
    public int getCurrentStage() { return currentStage; }
    public long getSeed() { return seed; }
    public int getStageCount() { return stageCount; }
    public String getPreviousNodeId() { return previousNodeId; }
    public String getPendingNodeId() { return pendingNodeId; }
    public boolean isFinalNode(Node node) { return node != null && node.type == Type.BOSS; }

    public List<Node> getReachableNodes() {
        if (pendingNodeId != null) return List.of();
        if (previousNodeId == null) return List.of(nodes.get("tutorial"));
        Node previous = nodes.get(previousNodeId);
        if (previous == null) return List.of();
        List<Node> result = new ArrayList<>();
        for (String id : previous.next) {
            Node next = nodes.get(id);
            if (next != null && next.stage == currentStage) result.add(next);
        }
        return result;
    }

    public boolean enter(String id) {
        for (Node node : getReachableNodes()) {
            if (node.id.equals(id)) { pendingNodeId = id; return true; }
        }
        return false;
    }

    public Node getPendingNode() { return nodes.get(pendingNodeId); }

    public void completePendingNode() {
        Node node = nodes.get(pendingNodeId);
        if (node == null) return;
        previousNodeId = node.id;
        currentStage = node.stage + 1;
        pendingNodeId = null;
    }

    public void interrupted() {
        Node pending = nodes.get(pendingNodeId);
        if (pending != null && pending.type != Type.SHOP) pendingNodeId = null;
    }

    public Map<String, Object> toData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stageCount", stageCount); data.put("seed", seed); data.put("currentStage", currentStage);
        data.put("previousNodeId", previousNodeId); data.put("pendingNodeId", pendingNodeId);
        List<Object> serialized = new ArrayList<>();
        for (Node node : nodes.values()) {
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("id", node.id); n.put("title", node.title); n.put("stage", node.stage); n.put("type", node.type.name());
            n.put("next", new ArrayList<>(node.next)); n.put("shopStock", node.shopStock);
            n.put("purchased", new ArrayList<>(node.purchased)); serialized.add(n);
        }
        data.put("nodes", serialized);
        return data;
    }

    public static RunRoute fromData(Map<String, Object> data) {
        if (data == null) return null;
        int stages = data.get("stageCount") instanceof Number ? ((Number) data.get("stageCount")).intValue() : DEFAULT_STAGES;
        long seed = data.get("seed") instanceof Number ? ((Number) data.get("seed")).longValue() : 0L;
        RunRoute route = new RunRoute(stages, seed);
        route.currentStage = data.get("currentStage") instanceof Number ? ((Number) data.get("currentStage")).intValue() : 0;
        route.previousNodeId = data.get("previousNodeId") instanceof String ? (String) data.get("previousNodeId") : null;
        route.pendingNodeId = data.get("pendingNodeId") instanceof String ? (String) data.get("pendingNodeId") : null;
        Object list = data.get("nodes");
        if (!(list instanceof List)) return null;
        for (Object value : (List<?>) list) {
            if (!(value instanceof Map)) continue;
            Map<?, ?> m = (Map<?, ?>) value;
            String id = m.get("id") instanceof String ? (String) m.get("id") : null;
            String title = m.get("title") instanceof String ? (String) m.get("title") : id;
            int stage = m.get("stage") instanceof Number ? ((Number) m.get("stage")).intValue() : 0;
            Type type;
            try { type = Type.valueOf((String) m.get("type")); } catch (RuntimeException ex) { continue; }
            if (id == null) continue;
            Node node = new Node(id, title, stage, type);
            if (m.get("next") instanceof List) for (Object next : (List<?>) m.get("next")) if (next instanceof String) node.next.add((String) next);
            if (m.get("shopStock") instanceof List) for (Object item : (List<?>) m.get("shopStock")) if (item instanceof Map) node.shopStock.add(stringMap((Map<?, ?>) item));
            if (m.get("purchased") instanceof List) for (Object item : (List<?>) m.get("purchased")) if (item instanceof String) node.purchased.add((String) item);
            route.nodes.put(id, node);
        }
        return route.nodes.isEmpty() ? null : route;
    }

    private static Map<String, Object> stringMap(Map<?, ?> value) {
        Map<String, Object> result = new LinkedHashMap<>();
        value.forEach((k, v) -> { if (k instanceof String) result.put((String) k, v); });
        return result;
    }
}
