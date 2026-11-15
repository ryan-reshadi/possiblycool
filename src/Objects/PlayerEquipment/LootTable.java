package Objects.PlayerEquipment;

import Objects.PlayerClasses.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/** Weighted item pool that only rolls entries usable by the current player. */
public final class LootTable {
    private static final Random RANDOM = new Random();

    private static final class Entry {
        final Supplier<? extends Item> factory;
        final int weight;

        Entry(int weight, Supplier<? extends Item> factory) {
            if (weight <= 0) throw new IllegalArgumentException("Loot weights must be positive.");
            this.weight = weight;
            this.factory = factory;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    @SafeVarargs
    public static LootTable of(WeightedLoot... entries) {
        LootTable table = new LootTable();
        if (entries != null) {
            for (WeightedLoot entry : entries) table.add(entry.weight, entry.factory);
        }
        return table;
    }

    public LootTable add(int weight, Supplier<? extends Item> factory) {
        if (factory == null) throw new IllegalArgumentException("Loot item factory cannot be null.");
        entries.add(new Entry(weight, factory));
        return this;
    }

    public List<Item> roll(Player player, int count) {
        List<Entry> eligible = new ArrayList<>();
        for (Entry entry : entries) {
            Item sample;
            try {
                sample = entry.factory.get();
            } catch (RuntimeException ex) {
                continue;
            }
            if (sample != null && sample.canBeUsedBy(player)) eligible.add(entry);
        }
        List<Item> result = new ArrayList<>();
        if (eligible.isEmpty() || count <= 0) return result;
        for (int i = 0; i < count; i++) {
            int totalWeight = 0;
            for (Entry entry : eligible) totalWeight += entry.weight;
            int choice = RANDOM.nextInt(totalWeight);
            for (Entry entry : eligible) {
                choice -= entry.weight;
                if (choice < 0) {
                    Item item = entry.factory.get();
                    if (item != null && item.canBeUsedBy(player)) {
                        if (item instanceof PlayerWeapon) {
                            PlayerWeapon weapon = (PlayerWeapon) item;
                            Item equipped = player == null ? null : player.getEquippedItem();
                            int referenceLevel = equipped instanceof PlayerWeapon
                                    ? ((PlayerWeapon) equipped).getLevel() : 1;
                            weapon.setLevel(PlayerWeapon.rollLevelNear(referenceLevel, RANDOM));
                        } else if (item instanceof Armor) {
                            Armor armor = (Armor) item;
                            Armor equipped = armor.getSlot() == Armor.ArmorSlot.HEAD
                                    ? player.getHeadArmor() : player.getBodyArmor();
                            armor.setLevel(Armor.rollLevelNear(equipped == null ? 1 : equipped.getLevel(), RANDOM));
                        }
                        result.add(item);
                    }
                    break;
                }
            }
        }
        return result;
    }

    public static final class WeightedLoot {
        private final int weight;
        private final Supplier<? extends Item> factory;

        private WeightedLoot(int weight, Supplier<? extends Item> factory) {
            this.weight = weight;
            this.factory = factory;
        }
    }

    public static WeightedLoot item(int weight, Supplier<? extends Item> factory) {
        return new WeightedLoot(weight, factory);
    }
}
