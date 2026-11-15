package Objects.PlayerEquipment;

import Levels.BaseLevel;
import Objects.EnemyClasses.Enemy;
import Objects.PlayerClasses.Player;
import Objects.PlayerClasses.RoguePlayer;
import Objects.PlayerClasses.TankPlayer;
import Objects.PlayerClasses.WizardPlayer;
import Objects.Effects.RallyBarricade;
import Objects.Effects.TricksterArrowEffect;
import java.awt.Color;
import java.util.List;

/** A class-specific, inventory-backed ultimate ability. */
public final class Ultimate extends Item {
    public enum Type {
        TRICKSTERS_ARROW("tricksters-arrow", "Trickster's Arrow", RoguePlayer.class),
        RALLY_BARRICADE("rally-barricade", "Rally Barricade", TankPlayer.class),
        WIND_GUST("wind-gust", "Wind Gust", WizardPlayer.class),
        ARCANE_NOVA("arcane-nova", "Arcane Nova", WizardPlayer.class),
        AETHER_WARD("aether-ward", "Aether Ward", WizardPlayer.class),
        EARTHSHATTER("earthshatter", "Earthshatter", TankPlayer.class),
        IRON_RESOLVE("iron-resolve", "Iron Resolve", TankPlayer.class),
        BLADE_TEMPEST("blade-tempest", "Blade Tempest", RoguePlayer.class),
        VANISH("vanish", "Vanish", RoguePlayer.class);

        private final String id;
        private final String displayName;
        private final Class<? extends Player> playerClass;

        Type(String id, String displayName, Class<? extends Player> playerClass) {
            this.id = id;
            this.displayName = displayName;
            this.playerClass = playerClass;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public boolean isUsableBy(Player player) { return playerClass.isInstance(player); }

        public static Type fromId(String id) {
            for (Type type : values()) if (type.id.equals(id)) return type;
            return null;
        }
    }

    private final Type type;

    public Ultimate(Type type) {
        this.type = java.util.Objects.requireNonNull(type, "type");
        this.name = type.displayName;
        this.description = descriptionFor(type);
        this.iconColor = colorFor(type);
        setAllowedClasses(type.playerClass);
    }

    public static Ultimate forId(String id) {
        Type type = Type.fromId(id);
        return type == null ? null : new Ultimate(type);
    }

    public static Type defaultFor(Player player) {
        if (player instanceof WizardPlayer) return Type.WIND_GUST;
        if (player instanceof TankPlayer) return Type.RALLY_BARRICADE;
        if (player instanceof RoguePlayer) return Type.TRICKSTERS_ARROW;
        throw new IllegalArgumentException("Unsupported player class: " + player.getClass().getName());
    }

    public static Type alternateFor(Player player) {
        if (player instanceof WizardPlayer) return Type.AETHER_WARD;
        if (player instanceof TankPlayer) return Type.IRON_RESOLVE;
        if (player instanceof RoguePlayer) return Type.VANISH;
        throw new IllegalArgumentException("Unsupported player class: " + player.getClass().getName());
    }

    public static String migrateLegacyStarterId(String id, Player player) {
        if (id == null) return null;
        if (player instanceof WizardPlayer && "arcane-nova".equals(id)) return Type.WIND_GUST.getId();
        if (player instanceof TankPlayer && "earthshatter".equals(id)) return Type.RALLY_BARRICADE.getId();
        if (player instanceof RoguePlayer && "blade-tempest".equals(id)) return Type.TRICKSTERS_ARROW.getId();
        return id;
    }

    public Type getType() { return type; }
    public String getId() { return type.id; }

    public void activate(Player player, BaseLevel level, int currentTick, int targetX, int targetY) {
        int px = player.getX() + player.getWidth() / 2;
        int py = player.getY() + player.getHeight() / 2;
        List<Enemy> enemies = level.getEnemies();
        switch (type) {
            case TRICKSTERS_ARROW:
                TricksterArrowEffect arrow = new TricksterArrowEffect(px, py, targetX, targetY, enemies,
                        player);
                level.queueAddVisualObject(arrow);
                break;
            case RALLY_BARRICADE:
                level.queueAddVisualObject(new RallyBarricade(targetX, targetY, player, enemies, currentTick));
                break;
            case WIND_GUST:
                double aim = Math.atan2(targetY - py, targetX - px);
                for (Enemy enemy : enemies) {
                    if (enemy == null || enemy.isDead()) continue;
                    int ex = enemy.getX() + enemy.getWidth() / 2;
                    int ey = enemy.getY() + enemy.getHeight() / 2;
                    double dx = ex - px, dy = ey - py;
                    double distance = Math.hypot(dx, dy);
                    double delta = Math.atan2(Math.sin(Math.atan2(dy, dx) - aim), Math.cos(Math.atan2(dy, dx) - aim));
                    if (distance <= 250 && Math.abs(delta) <= Math.toRadians(30)) {
                        int previousHealth = enemy.getHealth();
                        enemy.hit(20, px, py, 80, currentTick);
                        enemy.stun(60, currentTick);
                        player.chargeUltimateForDamage(previousHealth - enemy.getHealth());
                    }
                }
                break;
            case ARCANE_NOVA:
                hitNearby(player, enemies, px, py, 190, 75, currentTick, 10.0);
                break;
            case AETHER_WARD:
            case IRON_RESOLVE:
                player.grantUltimateGuard(currentTick, 360, 0.40);
                break;
            case EARTHSHATTER:
                for (Enemy enemy : enemies) {
                    if (enemy == null || enemy.isDead()) continue;
                    int ex = enemy.getX() + enemy.getWidth() / 2;
                    int ey = enemy.getY() + enemy.getHeight() / 2;
                    if (Math.hypot(ex - px, ey - py) <= 145) {
                        int previousHealth = enemy.getHealth();
                        enemy.hit(45, px, py, 75, currentTick);
                        player.chargeUltimateForDamage(previousHealth - enemy.getHealth());
                        enemy.stun(120, currentTick);
                    }
                }
                break;
            case BLADE_TEMPEST:
                hitNearby(player, enemies, px, py, 150, 55, currentTick, 8.0);
                break;
            case VANISH:
                player.grantUltimateGuard(currentTick, 150, 0.60);
                double dx = targetX - px;
                double dy = targetY - py;
                double distance = Math.hypot(dx, dy);
                if (distance > 0) {
                    double dash = Math.min(180, distance);
                    level.movePlayerInWorld((int) Math.round(dx / distance * dash),
                            (int) Math.round(dy / distance * dash));
                }
                break;
            default:
                break;
        }
    }

    private static void hitNearby(Player player, List<Enemy> enemies, int x, int y, int radius,
                                  int damage, int tick, double knockback) {
        for (Enemy enemy : enemies) {
            if (enemy == null || enemy.isDead()) continue;
            int ex = enemy.getX() + enemy.getWidth() / 2;
            int ey = enemy.getY() + enemy.getHeight() / 2;
            if (Math.hypot(ex - x, ey - y) <= radius) {
                int previousHealth = enemy.getHealth();
                enemy.hit(damage, x, y, knockback, tick);
                player.chargeUltimateForDamage(previousHealth - enemy.getHealth());
            }
        }
    }

    private static String descriptionFor(Type type) {
        switch (type) {
            case TRICKSTERS_ARROW: return "Chain an arrow between up to five enemies, adding 10 damage per hit.";
            case RALLY_BARRICADE: return "Create an 8-second projectile-blocking barricade. It heals you and damages enemies crossing it.";
            case WIND_GUST: return "Blast a 250px cone for 20 damage, knockback, and a 1-second stun.";
            case ARCANE_NOVA: return "Deal 75 damage to nearby enemies in a 190px radius.";
            case AETHER_WARD: return "Reduce incoming damage by 40% for 6 seconds.";
            case EARTHSHATTER: return "Deal 45 damage and stun nearby enemies.";
            case IRON_RESOLVE: return "Reduce incoming damage by 40% for 6 seconds.";
            case BLADE_TEMPEST: return "Deal 55 damage to nearby enemies in a 150px radius.";
            case VANISH: return "Dash toward the cursor and reduce incoming damage by 60% for 2.5 seconds.";
            default: return "A powerful class ultimate.";
        }
    }

    private static Color colorFor(Type type) {
        switch (type) {
            case TRICKSTERS_ARROW: return new Color(220, 190, 70);
            case RALLY_BARRICADE: return new Color(190, 130, 70);
            case WIND_GUST: return new Color(100, 200, 230);
            case ARCANE_NOVA: return new Color(145, 75, 225);
            case AETHER_WARD: return new Color(90, 190, 240);
            case EARTHSHATTER: return new Color(180, 115, 60);
            case IRON_RESOLVE: return new Color(190, 190, 205);
            case BLADE_TEMPEST: return new Color(220, 55, 75);
            case VANISH: return new Color(85, 220, 180);
            default: return Color.MAGENTA;
        }
    }
}
