package Objects.EnemyClasses;

/** Dash attack with a fixed tell and damage path. */
public class BerserkerWeapon extends EnemyWeapon {
    private boolean hitThisDash;
    public BerserkerWeapon() { super(18, 150, 220, 16); }
    @Override public void aim(Enemy owner, Objects.PlayerClasses.Player target, java.awt.Graphics2D g) {
        if (target == null) return;
        double a = owner.dashTellTicks > 0 ? owner.tellAngle
                : Math.atan2(target.getY()-owner.y, target.getX()-owner.x);
        g.setColor(java.awt.Color.DARK_GRAY); g.setStroke(new java.awt.BasicStroke(5));
        g.drawLine(owner.x+owner.width/2, owner.y+owner.height/2,
                (int)(owner.x+owner.width/2+Math.cos(a)*24), (int)(owner.y+owner.height/2+Math.sin(a)*24));
        g.setStroke(new java.awt.BasicStroke(1));
    }
    @Override public void update(Enemy owner, Objects.PlayerClasses.Player target, int tick) {
        if (target == null) return;
        if (owner.dashTicks > 0) {
            double nx = Math.cos(owner.dashAngle), ny = Math.sin(owner.dashAngle);
            owner.moveWithWallCollision((int)Math.round(nx*owner.dashSpeed), (int)Math.round(ny*owner.dashSpeed));
            if (!hitThisDash && target.getHitbox().intersects(owner.getHitbox())) { target.hurt(damage); hitThisDash=true; }
            owner.dashTicks--;
            if (owner.dashTicks == 0) owner.dashTellTicks = 0;
        } else if (owner.dashTellTicks > 0) {
            owner.dashTellTicks--;
            if(owner.dashTellTicks==0){owner.dashTicks=24;owner.dashSpeed=range/24.0;hitThisDash=false;lastAttack=tick;}
        } else if (ready(tick)) {
            owner.tellAngle = Math.atan2(target.getY()-owner.y, target.getX()-owner.x);
            owner.dashAngle=owner.tellAngle; owner.tellLength=(int)range; owner.dashTellTicks=36;
        }
    }
}
