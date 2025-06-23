package Objects.PlayerClasses;

public class RoguePlayer extends Player {

    public RoguePlayer(int x, int y) {
        super(x, y, 75);
        this.rollSpeed = 1.5;
        this.setImgPath("src/Images/rogue-player.png");
    }

    @Override
    public void attack() {

    }

    
    
}
