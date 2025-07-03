package Levels;

import Objects.PlayerClasses.*;
import java.util.ArrayList;
public class Level1 extends BaseLevel {
    
    public Level1(int bgX, int bgY, String path, Player player) {
        super(bgX, bgY, path, new ArrayList<Player>() {{ add(player); }});
        this.width = 800; // Example width
        this.height = 600; // Example height
        this.initLevel(); // Initialize level-specific elements
    }

    @Override
    protected void initLevel() {
        // Initialize specific visual objects for Level 1
        // For example, add enemies, items, etc.
    }
    
}
