package Objects.EnemyClasses;

import Objects.PlayerClasses.Player;
import java.awt.Color;
import java.awt.Graphics2D;
import Objects.PlayerEquipment.WeaponProfile;

/** A weapon owns an enemy's attack cadence and its visible aim. */
public abstract class EnemyWeapon {
    protected int cooldown;
    protected int lastAttack = -1;
    protected int damage;
    protected double range;
    protected double angle;
    protected final WeaponProfile profile;

    protected EnemyWeapon(int damage, int cooldown, double range, double angle) {
        this.damage = damage; this.cooldown = cooldown; this.range = range; this.angle = angle;
        this.profile = new WeaponProfile(damage, (int)range, (int)angle, 0, cooldown, 0);
    }

    public WeaponProfile getProfile() { return profile; }

    public abstract void update(Enemy owner, Player target, int tick);
    public void aim(Enemy owner, Player target, Graphics2D g) {
        if (target == null) return;
        double ox = owner.x + owner.width / 2.0, oy = owner.y + owner.height / 2.0;
        double a = Math.atan2(target.getY() + target.getHeight()/2.0 - oy, target.getX() + target.getWidth()/2.0 - ox);
        g.setColor(new Color(35, 35, 40)); g.setStroke(new java.awt.BasicStroke(5));
        g.drawLine((int)ox, (int)oy, (int)(ox + Math.cos(a)*24), (int)(oy + Math.sin(a)*24));
        g.setStroke(new java.awt.BasicStroke(1));
    }
    protected boolean ready(int tick) { return lastAttack < 0 || tick >= lastAttack + cooldown; }
}
