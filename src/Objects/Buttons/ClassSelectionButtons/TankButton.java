package Objects.Buttons.ClassSelectionButtons;

import GameRun.Game;
import GameRun.GameStates;
import Objects.Buttons.Button;

public class TankButton extends Button {
    public TankButton(int x, int y, int width, int height, Game game) {
        super(x, y, width, height, "Tank", "#CECECE", game);
    }

    @Override
    public void buttonAction() {
        this.game.player = new Objects.PlayerClasses.TankPlayer(400, 300);
        this.game.changeState(GameStates.LEVEL_1);
    }
}
