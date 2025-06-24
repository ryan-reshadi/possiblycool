package Objects.Buttons;

import GameRun.Game;
import GameRun.GameStates;

public class StartButton extends Button {
    public StartButton(int x, int y, int width, int height, Game game) {
        super(x, y, width, height, "Start Game", "#00FF00", game);
    }

    @Override
    public void buttonAction() {
        this.game.changeState(GameStates.GAME_STARTED_0);
    }
}
