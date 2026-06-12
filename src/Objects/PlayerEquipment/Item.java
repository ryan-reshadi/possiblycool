package Objects.PlayerEquipment;

import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import java.util.ArrayList;

public abstract class Item {
	public Item(boolean visible, String imgpath) {
		if (!visible) {
			throw new IllegalArgumentException("Bad Input; This constructor is ONLY for visible items, first argument should be true");
		}
	}
	public Item() {
		
	}
	public void tick(int currentTick, int clickXDown, int clickYDown, int clickXUp, int clickYUp, ArrayList<VisualObject> targets, Player owner) {
    	if (clickXDown>-1 || clickYDown>-1) {
    		this.leftClickAct(clickXDown, clickYDown, clickXUp, clickYUp, currentTick, owner.getX()+owner.getWidth()/2, owner.getY()+owner.getHeight());
    	}
    }
	
	public void leftClickAct(int clickXDown, int clickYDown, int clickXUp, int clickYUp, int currentTick, int processedX, int processedY) {
		
		
	}
}
