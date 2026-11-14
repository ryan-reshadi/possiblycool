package Objects.EnemyClasses;
import Objects.PlayerClasses.Player;

public class RangedWeapon extends EnemyWeapon {
    private final double projectileSpeed;

    public RangedWeapon() {
        this(9, 90, 360, 6.0);
    }

    public RangedWeapon(int damage, int cooldown, double range, double projectileSpeed) {
        super(damage, cooldown, range, 0);
        this.projectileSpeed = projectileSpeed;
    }

    @Override
    public void update(Enemy enemy, Player player, int tick) {
        if (player == null) return;

        double dx = player.getX() - enemy.x;
        double dy = player.getY() - enemy.y;
        double distance = Math.hypot(dx, dy);
        if (distance < range && ready(tick) && enemy.hasLineOfSightTo(player)
                && enemy.projectileSink != null) {
            enemy.projectileSink.accept(new EnemyProjectile(
                    enemy.x + enemy.width / 2, enemy.y + enemy.height / 2,
                    player, damage, projectileSpeed, enemy.borders));
            lastAttack = tick;
        } else if (distance < 100) {
            enemy.moveWithWallCollision(
                    (int) Math.round(-dx / Math.max(1, distance)),
                    (int) Math.round(-dy / Math.max(1, distance)));
        }
    }
}
