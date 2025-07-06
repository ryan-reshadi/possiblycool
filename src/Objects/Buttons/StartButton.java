package Objects.Buttons;

import GameRun.Game;
import GameRun.GameStates;
import java.awt.Color;

public class StartButton extends Button {
    public StartButton(int x, int y, int width, int height, Game game) {
        super(x, y, width, height, "Start Game", Color.decode("#00FF00"), game);
    }

    @Override
    public void buttonAction() {
        this.game.changeState(GameStates.GAME_STARTED_0);
    }
}
