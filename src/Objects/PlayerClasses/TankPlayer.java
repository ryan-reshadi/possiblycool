package Objects.PlayerClasses;

import Objects.PlayerEquipment.Item;

public class TankPlayer extends Player {

    public TankPlayer(int x, int y) {
        super(x, y, 150);
        this.rollSpeed = 0.5;
        this.setImgPath("images/player-tank.jpg");
    }

    public void giveWeapon(){
        this.inventory[0] = new Item();
    }

    public void testWorking() {
    	System.out.println("Tank player is working!");
    }
    
}
