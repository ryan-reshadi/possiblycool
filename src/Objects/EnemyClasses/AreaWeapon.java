package Objects.EnemyClasses;
import Objects.PlayerClasses.Player;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/** Telegraphs a circular blast before resolving damage. */
public class AreaWeapon extends EnemyWeapon {
    private int windup;
    public AreaWeapon(){super(22,180,115,360);}
    @Override public void update(Enemy e,Player p,int tick){
        if (p == null) return;
        if (windup > 0) {
            if (--windup == 0) {
                double cx = e.x + e.width / 2.0, cy = e.y + e.height / 2.0;
                double distance = Math.hypot(p.getX() + p.getWidth() / 2.0 - cx,
                        p.getY() + p.getHeight() / 2.0 - cy);
                if (e.hasLineOfSightTo(p) && distance <= range) p.hurt(damage);
                lastAttack = tick;
            }
        } else if (ready(tick) && e.hasLineOfSightTo(p)) {
            windup = 48;
        }
    }
    @Override public void aim(Enemy e,Player p,Graphics2D g){
        if(windup>0){g.setColor(new Color(255,70,30,90));double d=range*2;g.fill(new Ellipse2D.Double(e.x+e.width/2.0-range,e.y+e.height/2.0-range,d,d));}
        else super.aim(e,p,g);
    }
}
