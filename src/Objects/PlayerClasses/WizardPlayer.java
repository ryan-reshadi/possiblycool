package Objects.PlayerClasses;

public class WizardPlayer extends Player {

    public WizardPlayer(int x, int y) {
        super(x, y, 100);
        this.setImgPath("images/player-wizard.jpg");
        this.rollSpeed = 1.0;
    }

    @Override
    public void attack() {
        
    }
    
}
