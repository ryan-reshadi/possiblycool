package Levels;

import Objects.PlayerClasses.*;
import java.awt.Color;
import java.util.ArrayList;
public class Level1 extends BaseLevel {
    
    public Level1(int bgX, int bgY, Player player) {
        super(Color.BLACK, new ArrayList<Player>() {{ add(player); }});
        this.width = 800; // Example width
        this.height = 600; // Example height
        this.initLevel(); // Initialize level-specific elements
    }

    @Override
    protected void initLevel() {
        this.levelVisualObjects.add(new ArrayList<>()); // Initialize with one empty row
        
    }
    
}
