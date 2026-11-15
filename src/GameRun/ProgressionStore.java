package GameRun;

import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Armor;
import Objects.PlayerEquipment.Consumable;
import Objects.PlayerEquipment.HealthPotion;
import Objects.PlayerEquipment.Item;
import Objects.PlayerEquipment.PlayerWeapon;
import Objects.PlayerEquipment.Ultimate;
import Objects.PlayerEquipment.TankSword;
import Objects.PlayerEquipment.KnightChestplate;
import Objects.PlayerEquipment.KnightHelmet;
import Objects.PlayerEquipment.RogueHood;
import Objects.PlayerEquipment.RogueTunic;
import Objects.PlayerEquipment.WizardHat;
import Objects.PlayerEquipment.WizardRobe;
import Objects.PlayerEquipment.Weapons.BasicWand;
import Objects.PlayerEquipment.Weapons.Dagger;
import Objects.PlayerEquipment.Weapons.Hammer;
import Objects.PlayerEquipment.Weapons.Wand;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/** Stores durable, per-character campaign progress and loadouts in one local JSON profile. */
public final class ProgressionStore {
    private static final String LEGACY_LEVEL_SELECTOR_UNLOCKED = "levelSelectorUnlocked";
    private final Path saveFile;
    private final Preferences legacyPreferences;
    private final Map<String, CharacterProgress> progress = new LinkedHashMap<>();

    public ProgressionStore() {
        this(defaultSaveFile(), Preferences.userNodeForPackage(ProgressionStore.class));
    }

    ProgressionStore(Preferences preferences) {
        this(defaultSaveFile(), preferences);
    }

    ProgressionStore(Path saveFile) {
        this(saveFile, null);
    }

    ProgressionStore(Path saveFile, Preferences preferences) {
        this.saveFile = java.util.Objects.requireNonNull(saveFile, "saveFile");
        this.legacyPreferences = preferences;
        load();
    }

    private static Path defaultSaveFile() {
        String home = System.getProperty("user.home");
        if (home == null || home.isBlank()) {
            throw new IllegalStateException("Could not determine the user's home directory for saves.");
        }
        return Paths.get(home, ".possiblycool", "player-progress.json");
    }

    public synchronized boolean isLevelSelectorUnlocked() {
        if (!completedLevelsAcrossCharacters().isEmpty()) return true;
        return legacyPreferences != null && legacyPreferences.getBoolean(LEGACY_LEVEL_SELECTOR_UNLOCKED, false);
    }

    public synchronized void unlockLevelSelector() {
        if (getProgress("rogue").completedLevels.add("level-1")) write();
        if (legacyPreferences != null) {
            legacyPreferences.putBoolean(LEGACY_LEVEL_SELECTOR_UNLOCKED, true);
            try {
                legacyPreferences.flush();
            } catch (BackingStoreException e) {
                throw new IllegalStateException("Could not migrate level progression.", e);
            }
        }
    }

    public synchronized void importLegacyLevelSelectorUnlock() {
        if (legacyPreferences != null && legacyPreferences.getBoolean(LEGACY_LEVEL_SELECTOR_UNLOCKED, false)
                && getProgress("rogue").completedLevels.add("level-1")) {
            write();
        }
    }

    public synchronized void completeLevel(Player player, String levelId) {
        java.util.Objects.requireNonNull(player, "player");
        completeLevel(characterId(player), levelId);
    }

    public synchronized boolean hasCompletedLevel(Player player, String levelId) {
        return getProgress(characterId(player)).completedLevels.contains(levelId);
    }

    public synchronized Set<String> getCompletedLevels(Player player) {
        return Set.copyOf(getProgress(characterId(player)).completedLevels);
    }

    public synchronized int getCoins(Player player) {
        return getProgress(characterId(player)).coins;
    }

    public synchronized int addCoins(Player player, int amount) {
        if (amount <= 0) return getCoins(player);
        CharacterProgress character = getProgress(characterId(player));
        character.coins = (int) Math.min(Integer.MAX_VALUE, (long) character.coins + amount);
        write();
        return character.coins;
    }

    public synchronized Map<String, Object> getRunState(Player player) {
        return getProgress(characterId(player)).activeRun;
    }

    public synchronized void saveRunState(Player player, Map<String, Object> state) {
        getProgress(characterId(player)).activeRun = state == null ? null : new LinkedHashMap<>(state);
        write();
    }

    public synchronized void clearRunState(Player player) {
        getProgress(characterId(player)).activeRun = null;
        write();
    }

    public synchronized void addStoredItem(Player player, Item item) {
        if (item == null) return;
        CharacterProgress character = getProgress(characterId(player));
        if (item instanceof Ultimate && ownsUltimate(player, ((Ultimate) item).getId())) return;
        character.storage.add(serializeItem(item));
        write();
    }

    public synchronized boolean ownsUltimate(Player player, String ultimateId) {
        CharacterProgress character = getProgress(characterId(player));
        if (ultimateId == null) return false;
        if (player.getUltimate() != null && ultimateId.equals(player.getUltimate().getId())) return true;
        if (character.unlockedUltimates.contains(ultimateId)
                || (character.equippedUltimateId != null && character.equippedUltimateId.equals(ultimateId))) return true;
        for (Map<String, Object> item : character.storage) {
            if ("ultimate".equals(item.get("type")) && ultimateId.equals(item.get("id"))) return true;
        }
        return false;
    }

    public synchronized boolean purchaseUltimate(Player player, Ultimate.Type type, int cost) {
        Ultimate item = new Ultimate(type);
        if (!item.canBeUsedBy(player) || ownsUltimate(player, item.getId()) || cost < 0) return false;
        CharacterProgress character = getProgress(characterId(player));
        if (character.coins < cost) return false;
        character.coins -= cost;
        character.unlockedUltimates.add(item.getId());
        character.storage.add(serializeItem(item));
        write();
        return true;
    }

    public synchronized boolean purchaseItem(Player player, Item item, int cost) {
        if (item == null || item instanceof Ultimate || !item.canBeUsedBy(player) || cost < 0) return false;
        CharacterProgress character = getProgress(characterId(player));
        if (character.coins < cost) return false;
        character.coins -= cost;
        character.storage.add(serializeItem(item));
        write();
        return true;
    }

    static Item deserializeShopItem(Map<String, Object> data) {
        return deserializeItem(data);
    }

    public synchronized void savePlayer(Player player) {
        java.util.Objects.requireNonNull(player, "player");
        CharacterProgress character = getProgress(characterId(player));
        character.inventory.clear();
        for (Item item : player.getInventory()) character.inventory.add(serializeItem(item));
        character.headArmor = serializeItem(player.getHeadArmor());
        character.bodyArmor = serializeItem(player.getBodyArmor());
        character.mainHandIndex = player.getMainHandIndex();
        character.equippedUltimateId = player.getUltimate() == null ? null : player.getUltimate().getId();
        write();
    }

    public synchronized void savePlayer(Player player, List<Item> storedItems) {
        CharacterProgress character = getProgress(characterId(player));
        character.inventory.clear();
        for (Item item : player.getInventory()) character.inventory.add(serializeItem(item));
        character.headArmor = serializeItem(player.getHeadArmor());
        character.bodyArmor = serializeItem(player.getBodyArmor());
        character.mainHandIndex = player.getMainHandIndex();
        character.equippedUltimateId = player.getUltimate() == null ? null : player.getUltimate().getId();
        character.storage.clear();
        for (Item item : storedItems) character.storage.add(serializeItem(item));
        write();
    }

    public synchronized void restorePlayer(Player player) {
        java.util.Objects.requireNonNull(player, "player");
        CharacterProgress character = getProgress(characterId(player));
        if (character.inventory.isEmpty() && character.headArmor == null && character.bodyArmor == null
                && character.equippedUltimateId == null) return;

        Item[] inventory = new Item[player.getInventory().length];
        for (int i = 0; i < Math.min(inventory.length, character.inventory.size()); i++) {
            inventory[i] = deserializeItem(character.inventory.get(i));
        }
        player.setInventory(inventory);
        player.setMainHandIndex(Math.max(0, Math.min(inventory.length - 1, character.mainHandIndex)));
        player.equipHead(asArmor(deserializeItem(character.headArmor), Armor.ArmorSlot.HEAD));
        player.equipBody(asArmor(deserializeItem(character.bodyArmor), Armor.ArmorSlot.BODY));
        String savedUltimateId = Ultimate.migrateLegacyStarterId(character.equippedUltimateId, player);
        Ultimate restoredUltimate = Ultimate.forId(savedUltimateId);
        if (restoredUltimate != null && restoredUltimate.canBeUsedBy(player)) player.setUltimate(restoredUltimate);
    }

    public synchronized boolean unlockAlternateUltimate(Player player) {
        return unlockUltimate(player, Ultimate.alternateFor(player));
    }

    public synchronized boolean unlockUltimate(Player player, Ultimate.Type type) {
        CharacterProgress character = getProgress(characterId(player));
        Ultimate alternate = new Ultimate(type);
        if (!alternate.canBeUsedBy(player)) {
            throw new IllegalArgumentException("Ultimate does not belong to " + player.getClass().getSimpleName());
        }
        if (!character.unlockedUltimates.add(alternate.getId())) return false;
        character.storage.add(serializeItem(alternate));
        write();
        return true;
    }

    public synchronized List<Item> getStoredItems(Player player) {
        CharacterProgress character = getProgress(characterId(player));
        List<Item> items = new ArrayList<>();
        for (Map<String, Object> item : character.storage) {
            Item restored = deserializeItem(item);
            if (restored != null) items.add(restored);
        }
        return items;
    }

    public synchronized void saveStoredItems(Player player, List<Item> storedItems) {
        CharacterProgress character = getProgress(characterId(player));
        character.storage.clear();
        for (Item item : storedItems) character.storage.add(serializeItem(item));
        write();
    }

    private synchronized void completeLevel(String characterId, String levelId) {
        java.util.Objects.requireNonNull(levelId, "levelId");
        if (getProgress(characterId).completedLevels.add(levelId)) write();
    }

    private Set<String> completedLevelsAcrossCharacters() {
        Set<String> completed = new LinkedHashSet<>();
        for (CharacterProgress character : progress.values()) completed.addAll(character.completedLevels);
        return completed;
    }

    private CharacterProgress getProgress(String characterId) {
        return progress.computeIfAbsent(characterId, ignored -> new CharacterProgress());
    }

    private static String characterId(Player player) {
        String simpleName = player.getClass().getSimpleName();
        if ("RoguePlayer".equals(simpleName)) return "rogue";
        if ("WizardPlayer".equals(simpleName)) return "warlock";
        if ("TankPlayer".equals(simpleName)) return "tank";
        throw new IllegalArgumentException("Unsupported player class: " + simpleName);
    }

    private void load() {
        if (!Files.exists(saveFile)) return;
        try {
            String json = Files.readString(saveFile, StandardCharsets.UTF_8);
            Object rootValue = new JsonParser(json).parse();
            if (!(rootValue instanceof Map)) return;
            Object characters = ((Map<?, ?>) rootValue).get("characters");
            if (!(characters instanceof Map)) return;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) characters).entrySet()) {
                if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof Map)) continue;
                Map<?, ?> data = (Map<?, ?>) entry.getValue();
                CharacterProgress character = new CharacterProgress();
                Object levels = data.get("completedLevels");
                if (levels instanceof List) {
                    for (Object level : (List<?>) levels) if (level instanceof String) character.completedLevels.add((String) level);
                }
                Object items = data.get("inventory");
                if (items instanceof List) for (Object item : (List<?>) items) character.inventory.add(asMap(item));
                Object storedItems = data.get("storage");
                if (storedItems instanceof List) for (Object item : (List<?>) storedItems) character.storage.add(asMap(item));
                character.headArmor = asMap(data.get("headArmor"));
                character.bodyArmor = asMap(data.get("bodyArmor"));
                character.equippedUltimateId = stringValue(data.get("equippedUltimateId"));
                Object unlocked = data.get("unlockedUltimates");
                if (unlocked instanceof List) {
                    for (Object id : (List<?>) unlocked) if (id instanceof String) character.unlockedUltimates.add((String) id);
                }
                character.coins = intValue(data.get("coins"), 0);
                character.activeRun = asMap(data.get("activeRun"));
                Object hand = data.get("mainHandIndex");
                if (hand instanceof Number) character.mainHandIndex = ((Number) hand).intValue();
                progress.put((String) entry.getKey(), character);
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("Could not load player progression from " + saveFile, e);
        }
    }

    private void write() {
        StringBuilder json = new StringBuilder("{\n  \"version\": 1,\n  \"characters\": {");
        boolean firstCharacter = true;
        for (Map.Entry<String, CharacterProgress> entry : progress.entrySet()) {
            if (!firstCharacter) json.append(',');
            firstCharacter = false;
            CharacterProgress character = entry.getValue();
            json.append("\n    ").append(quote(entry.getKey())).append(": {\n      \"completedLevels\": ");
            appendValue(json, new ArrayList<>(character.completedLevels));
            json.append(",\n      \"inventory\": ");
            appendValue(json, character.inventory);
            json.append(",\n      \"storage\": ");
            appendValue(json, character.storage);
            json.append(",\n      \"headArmor\": "); appendValue(json, character.headArmor);
            json.append(",\n      \"bodyArmor\": "); appendValue(json, character.bodyArmor);
            json.append(",\n      \"equippedUltimateId\": "); appendValue(json, character.equippedUltimateId);
            json.append(",\n      \"unlockedUltimates\": "); appendValue(json, new ArrayList<>(character.unlockedUltimates));
            json.append(",\n      \"mainHandIndex\": ").append(character.mainHandIndex);
            json.append(",\n      \"coins\": ").append(character.coins);
            json.append(",\n      \"activeRun\": "); appendValue(json, character.activeRun);
            json.append("\n    }");
        }
        if (!firstCharacter) json.append('\n').append("  ");
        json.append("}\n}\n");

        Path parent = saveFile.toAbsolutePath().getParent();
        Path temporary = null;
        try {
            Files.createDirectories(parent);
            temporary = Files.createTempFile(parent, "player-progress-", ".tmp");
            Files.writeString(temporary, json, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, saveFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, saveFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save player progression to " + saveFile, e);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
            }
        }
    }

    static Map<String, Object> serializeItem(Item item) {
        if (item == null) return null;
        Map<String, Object> data = new LinkedHashMap<>();
        if (item instanceof Ultimate) { data.put("type", "ultimate"); data.put("id", ((Ultimate) item).getId()); }
        else if (item instanceof RogueHood) data.put("type", "rogue-hood");
        else if (item instanceof RogueTunic) data.put("type", "rogue-tunic");
        else if (item instanceof WizardHat) data.put("type", "wizard-hat");
        else if (item instanceof WizardRobe) data.put("type", "wizard-robe");
        else if (item instanceof KnightHelmet) data.put("type", "knight-helmet");
        else if (item instanceof KnightChestplate) data.put("type", "knight-chestplate");
        else if (item instanceof BasicWand) data.put("type", "basic-wand");
        else if (item instanceof Wand) data.put("type", "wand");
        else if (item instanceof Dagger) data.put("type", "dagger");
        else if (item instanceof Hammer) data.put("type", "hammer");
        else if (item instanceof TankSword) data.put("type", "tank-sword");
        else if (item instanceof HealthPotion) data.put("type", "health-potion");
        else throw new IllegalArgumentException("Unsupported item type for saving: " + item.getClass().getName());

        if (item instanceof Consumable) {
            Consumable consumable = (Consumable) item;
            data.put("stackCount", consumable.getStackCount());
            data.put("usesRemaining", consumable.getUsesRemaining());
        }
        if (item instanceof PlayerWeapon) {
            PlayerWeapon weapon = (PlayerWeapon) item;
            data.put("weaponLevel", weapon.getLevel());
            data.put("damageRoll", weapon.getDamageRollMultiplier());
        }
        if (item instanceof Armor) data.put("armorLevel", ((Armor) item).getLevel());
        if (item instanceof Wand) {
            Wand wand = (Wand) item;
            data.put("attackPattern", wand.getAttackPattern().name());
            data.put("firingCooldown", wand.getFiringCooldown());
            data.put("activeSpell", wand.getSavedActiveSpellType());
            data.put("passives", wand.getSavedPassiveSpellTypes());
        }
        return data;
    }

    private static Item deserializeItem(Map<String, Object> data) {
        if (data == null) return null;
        String type = stringValue(data.get("type"));
        Item item;
        switch (type) {
            case "ultimate": item = Ultimate.forId(stringValue(data.get("id"))); break;
            case "rogue-hood": item = new RogueHood(); break;
            case "rogue-tunic": item = new RogueTunic(); break;
            case "wizard-hat": item = new WizardHat(); break;
            case "wizard-robe": item = new WizardRobe(); break;
            case "knight-helmet": item = new KnightHelmet(); break;
            case "knight-chestplate": item = new KnightChestplate(); break;
            case "basic-wand": item = new BasicWand(); break;
            case "wand": item = new Wand("Enchanted Wand", intValue(data.get("firingCooldown"), 25),
                    wandPattern(data.get("attackPattern")),
                    data.get("damageRoll") instanceof Number ? ((Number) data.get("damageRoll")).doubleValue() : 1.0,
                    intValue(data.get("weaponLevel"), 1)); break;
            case "dagger": item = new Dagger(); break;
            case "hammer": item = new Hammer(); break;
            case "tank-sword": item = new TankSword(); break;
            case "health-potion": item = new HealthPotion(); break;
            default: return null;
        }
        if (item instanceof Consumable) {
            Consumable consumable = (Consumable) item;
            consumable.restoreState(intValue(data.get("stackCount"), 1), intValue(data.get("usesRemaining"), 1));
        }
        if (item instanceof PlayerWeapon) {
            PlayerWeapon weapon = (PlayerWeapon) item;
            Object roll = data.get("damageRoll");
            weapon.restoreProgression(roll instanceof Number ? ((Number) roll).doubleValue() : 1.0,
                    intValue(data.get("weaponLevel"), 1));
        }
        if (item instanceof Armor) ((Armor) item).restoreProgression(intValue(data.get("armorLevel"), 1));
        if (item instanceof Wand) restoreWand((Wand) item, data);
        return item;
    }

    private static void restoreWand(Wand wand, Map<String, Object> data) {
        String pattern = stringValue(data.get("attackPattern"));
        try { if (pattern != null) wand.setAttackPattern(Wand.AttackPattern.valueOf(pattern)); }
        catch (IllegalArgumentException ignored) { }
        wand.setFiringCooldown(intValue(data.get("firingCooldown"), wand.getFiringCooldown()));
        Object passiveData = data.get("passives");
        String firstPassive = null;
        String secondPassive = null;
        if (passiveData instanceof List) {
            List<?> passives = (List<?>) passiveData;
            firstPassive = passives.size() > 0 ? stringValue(passives.get(0)) : null;
            secondPassive = passives.size() > 1 ? stringValue(passives.get(1)) : null;
        }
        wand.restoreSavedSpells(stringValue(data.get("activeSpell")), firstPassive, secondPassive);
    }

    private static Wand.AttackPattern wandPattern(Object pattern) {
        try { return pattern instanceof String ? Wand.AttackPattern.valueOf((String) pattern) : Wand.AttackPattern.SINGLE; }
        catch (IllegalArgumentException ignored) { return Wand.AttackPattern.SINGLE; }
    }

    private static Armor asArmor(Item item, Armor.ArmorSlot slot) {
        return item instanceof Armor && ((Armor) item).getSlot() == slot ? (Armor) item : null;
    }

    private static Map<String, Object> asMap(Object value) {
        if (!(value instanceof Map)) return null;
        Map<String, Object> result = new LinkedHashMap<>();
        ((Map<?, ?>) value).forEach((key, item) -> { if (key instanceof String) result.put((String) key, item); });
        return result;
    }

    private static void appendValue(StringBuilder out, Object value) {
        if (value == null) { out.append("null"); return; }
        if (value instanceof String) { out.append(quote((String) value)); return; }
        if (value instanceof Number || value instanceof Boolean) { out.append(value); return; }
        if (value instanceof List) {
            out.append('['); boolean first = true;
            for (Object item : (List<?>) value) { if (!first) out.append(','); first = false; appendValue(out, item); }
            out.append(']'); return;
        }
        if (value instanceof Map) {
            out.append('{'); boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!first) out.append(','); first = false;
                out.append(quote(String.valueOf(entry.getKey()))).append(':'); appendValue(out, entry.getValue());
            }
            out.append('}'); return;
        }
        throw new IllegalArgumentException("Unsupported JSON value: " + value.getClass());
    }

    private static String quote(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default: if (c < 0x20) out.append(String.format("\\u%04x", (int) c)); else out.append(c);
            }
        }
        return out.append('"').toString();
    }

    private static String stringValue(Object value) { return value instanceof String ? (String) value : null; }
    private static int intValue(Object value, int fallback) { return value instanceof Number ? ((Number) value).intValue() : fallback; }

    private static final class CharacterProgress {
        final Set<String> completedLevels = new LinkedHashSet<>();
        final List<Map<String, Object>> inventory = new ArrayList<>();
        final List<Map<String, Object>> storage = new ArrayList<>();
        Map<String, Object> headArmor;
        Map<String, Object> bodyArmor;
        int mainHandIndex;
        int coins;
        Map<String, Object> activeRun;
        String equippedUltimateId;
        final Set<String> unlockedUltimates = new LinkedHashSet<>();
    }

    /** Small JSON parser to keep saves dependency-free. */
    private static final class JsonParser {
        private final String input;
        private int cursor;
        JsonParser(String input) { this.input = input; }
        Object parse() { Object value = readValue(); skipWhitespace(); if (cursor != input.length()) fail(); return value; }
        private Object readValue() {
            skipWhitespace(); if (cursor >= input.length()) return fail();
            char c = input.charAt(cursor);
            if (c == '{') return readObject(); if (c == '[') return readArray(); if (c == '"') return readString();
            if (c == 't') { expect("true"); return true; } if (c == 'f') { expect("false"); return false; }
            if (c == 'n') { expect("null"); return null; } return readNumber();
        }
        private Map<String, Object> readObject() {
            cursor++; Map<String, Object> result = new LinkedHashMap<>(); skipWhitespace(); if (take('}')) return result;
            do { skipWhitespace(); String key = readString(); skipWhitespace(); require(':'); result.put(key, readValue()); skipWhitespace(); if (take('}')) return result; require(','); } while (true);
        }
        private List<Object> readArray() {
            cursor++; List<Object> result = new ArrayList<>(); skipWhitespace(); if (take(']')) return result;
            do { result.add(readValue()); skipWhitespace(); if (take(']')) return result; require(','); } while (true);
        }
        private String readString() {
            require('"'); StringBuilder result = new StringBuilder();
            while (cursor < input.length()) { char c = input.charAt(cursor++); if (c == '"') return result.toString();
                if (c == '\\') { if (cursor >= input.length()) return fail(); char escaped = input.charAt(cursor++);
                    switch (escaped) { case '"': result.append('"'); break; case '\\': result.append('\\'); break; case '/': result.append('/'); break;
                        case 'b': result.append('\b'); break; case 'f': result.append('\f'); break; case 'n': result.append('\n'); break; case 'r': result.append('\r'); break; case 't': result.append('\t'); break;
                        case 'u': if (cursor + 4 > input.length()) return fail(); result.append((char) Integer.parseInt(input.substring(cursor, cursor + 4), 16)); cursor += 4; break; default: return fail(); }
                } else result.append(c);
            } return fail();
        }
        private Number readNumber() {
            int start = cursor; if (take('-')) { } while (cursor < input.length() && Character.isDigit(input.charAt(cursor))) cursor++;
            if (take('.')) while (cursor < input.length() && Character.isDigit(input.charAt(cursor))) cursor++;
            if (cursor < input.length() && (input.charAt(cursor) == 'e' || input.charAt(cursor) == 'E')) { cursor++; if (cursor < input.length() && (input.charAt(cursor) == '+' || input.charAt(cursor) == '-')) cursor++; while (cursor < input.length() && Character.isDigit(input.charAt(cursor))) cursor++; }
            if (start == cursor) return fail(); return Double.valueOf(input.substring(start, cursor));
        }
        private void expect(String value) { if (!input.startsWith(value, cursor)) fail(); cursor += value.length(); }
        private void skipWhitespace() { while (cursor < input.length() && Character.isWhitespace(input.charAt(cursor))) cursor++; }
        private boolean take(char c) { if (cursor < input.length() && input.charAt(cursor) == c) { cursor++; return true; } return false; }
        private void require(char c) { if (!take(c)) fail(); }
        private <T> T fail() { throw new IllegalArgumentException("Invalid player save JSON at offset " + cursor); }
    }
}
