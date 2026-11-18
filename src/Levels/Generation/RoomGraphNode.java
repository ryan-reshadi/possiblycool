package Levels.Generation;

public final class RoomGraphNode {
    public final int id;
    public final RoomRole role;
    public RoomTypeId assignedType;
    public RoomRestriction restrictionOverride; // null = use template/default
    public String pacingTier; // "EARLY", "MID", "CLIMAX"
    public String meta; // e.g. puzzle id

    public RoomGraphNode(int id, RoomRole role) {
        this.id = id;
        this.role = role;
        this.pacingTier = "MID";
    }
}
