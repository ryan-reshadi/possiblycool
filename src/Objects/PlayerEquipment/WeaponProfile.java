package Objects.PlayerEquipment;

import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

/** Shared immutable weapon move data and arc collision geometry. */
public final class WeaponProfile {
    public final int damage, range, arcDegrees, windupTicks, recoveryTicks;
    public final double knockback;
    public WeaponProfile(int damage, int range, int arcDegrees, int windupTicks, int recoveryTicks, double knockback) {
        this.damage=damage; this.range=range; this.arcDegrees=arcDegrees;
        this.windupTicks=windupTicks; this.recoveryTicks=recoveryTicks; this.knockback=knockback;
    }
    public Arc2D createArc(double cx,double cy,double angleRadians) {
        return new Arc2D.Double(cx-range,cy-range,range*2.0,range*2.0,
                -Math.toDegrees(angleRadians)-arcDegrees/2.0,arcDegrees,Arc2D.PIE);
    }
    public boolean hits(Arc2D arc,Rectangle2D target) {
        return arc.intersects(target)||arc.contains(target.getCenterX(),target.getCenterY());
    }
}
