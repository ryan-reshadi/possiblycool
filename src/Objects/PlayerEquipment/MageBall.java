package Objects.PlayerEquipment;

import Objects.VisualObject;
import java.util.ArrayList;

public class MageBall extends VisualObject{
    private double angle;
    private int speed;
    private double falloff= 0;
    public MageBall(double angle, int speed, double falloff, int x, int y){
        super(x, y, "wizardball.png");
        this.angle = angle;
        this.speed = speed;
        this.falloff = falloff;
    }
    public MageBall(double angle, int speed, int x, int y){
        super(x, y, "wizardball.png");
        this.angle = angle;
        this.speed = speed;
    }
    public void tick(){
        
        this.x += Math.cos(angle) * speed;
        this.y += Math.sin(angle) * speed;
        this.speed -= falloff;
    }

    public void remove(ArrayList<VisualObject> objects){
        objects.remove(this);
        this.visible = false;
    }
}
