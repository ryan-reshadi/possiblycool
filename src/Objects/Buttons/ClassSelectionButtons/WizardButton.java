package Objects.Buttons.ClassSelectionButtons;

import GameRun.Game;
import GameRun.GameStates;
import Objects.Buttons.Button;
import java.awt.Color;

public class WizardButton extends Button {
    public WizardButton(int x, int y, int width, int height, Game game) {
        super(x, y, width, height, "Wizard", Color.decode("#5DE2E7"), game);
    }

    @Override
    public void buttonAction() {
        this.game.player = new Objects.PlayerClasses.WizardPlayer(400, 300);
        this.game.changeState(GameStates.LEVEL_1);
    }
}
