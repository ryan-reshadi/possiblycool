package Objects.EnemyClasses;
import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import java.awt.Color;

public class RangedWeapon extends EnemyWeapon {
    private final double projectileSpeed;
    public RangedWeapon(){this(9,90,360,6.0);}
    public RangedWeapon(int damage,int cooldown,double range,double projectileSpeed){super(damage,cooldown,range,0);this.projectileSpeed=projectileSpeed;}
    @Override public void update(Enemy e,Player p,int tick){
        if(p==null)return;
        double dx=p.getX()-e.x,dy=p.getY()-e.y,d=Math.hypot(dx,dy);
        if(d<range && ready(tick) && e.projectileSink!=null){
            e.projectileSink.accept(new EnemyProjectile(e.x+e.width/2,e.y+e.height/2,p,damage,projectileSpeed)); lastAttack=tick;
        } else if(d<100){e.moveWithWallCollision((int)Math.round(-dx/Math.max(1,d)),(int)Math.round(-dy/Math.max(1,d)));}
    }
}
