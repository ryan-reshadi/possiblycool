package Objects.EnemyClasses;

import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import java.awt.*;
import java.util.Set;

final class EnemyProjectile extends VisualObject {
    private final Player target; private final double vx, vy; private final int damage; private int ttl=150;
    EnemyProjectile(int x,int y,Player target,int damage,double speed) {
        super(x,y,12,12,new Color(255,120,60)); this.target=target; this.damage=damage;
        double dx=target.getX()-x,dy=target.getY()-y,d=Math.max(1,Math.hypot(dx,dy)); vx=dx/d*speed;vy=dy/d*speed;
    }
    @Override public void tick(Graphics g,Set<Integer> keys,int a,int b,int c,int d,int tick) {
        x+=(int)Math.round(vx); y+=(int)Math.round(vy);
        if(--ttl<=0){visible=false;return;}
        if(target!=null && checkCollision(target)){target.hurt(damage);visible=false;return;}
        draw(g);
    }
}
