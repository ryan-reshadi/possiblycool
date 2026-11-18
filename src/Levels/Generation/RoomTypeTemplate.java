package Levels.Generation;

public final class RoomTypeTemplate {
    public final RoomTypeId id;
    public final int minW, maxW, minH, maxH;
    public final int preferredW, preferredH;
    public final String corridorWidthHint; // NARROW/NORMAL/WIDE (advisory)
    public final RoomRestriction defaultRestriction;
    public final RoomTags.RoomTag[] defaultTags;

    public RoomTypeTemplate(RoomTypeId id,
                            int minW, int maxW, int minH, int maxH,
                            int preferredW, int preferredH,
                            String corridorWidthHint,
                            RoomRestriction defaultRestriction,
                            RoomTags.RoomTag... defaultTags) {
        this.id = id;
        this.minW = minW; this.maxW = maxW; this.minH = minH; this.maxH = maxH;
        this.preferredW = preferredW; this.preferredH = preferredH;
        this.corridorWidthHint = corridorWidthHint == null ? "NORMAL" : corridorWidthHint;
        this.defaultRestriction = defaultRestriction == null ? RoomRestriction.NONE : defaultRestriction;
        this.defaultTags = defaultTags == null ? new RoomTags.RoomTag[0] : defaultTags;
    }
}
