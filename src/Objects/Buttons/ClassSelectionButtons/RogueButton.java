package Objects.Buttons.ClassSelectionButtons;

import GameRun.Game;
import GameRun.GameStates;
import Objects.Buttons.Button;


public class RogueButton extends Button {
    public RogueButton(int x, int y, int width, int height, Game game) {
        super(x, y, width, height, "Rogue", "#BFD641", game);
    }

    @Override
    public void buttonAction() {
        this.game.player = new Objects.PlayerClasses.RoguePlayer(400, 300);
        this.game.changeState(GameStates.LEVEL_1);
    }

}
