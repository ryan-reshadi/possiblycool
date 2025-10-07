package Levels;

import Objects.PlayerClasses.*;
import Objects.Terrain.Border;
import Objects.VisualObject;
import Objects.Buttons.*;
import Objects.EnemyClasses.*;

import java.awt.Color;
import java.util.ArrayList;
public class Level1 extends BaseLevel {
    
    public Level1(int bgX, int bgY, Player player) {
        super(Color.BLACK, new ArrayList<VisualObject>() {{ add(player); }});
        this.width = 800; // Example width
        this.height = 600; // Example height
        this.initLevel(); // Initialize level-specific elements
    }

    @Override
    protected void initLevel() {
        this.addBorder(new Border(300, 600, 200, 100, Color.PINK)); // Add player to the first row
        

    }    
}
