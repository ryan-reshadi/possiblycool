package Objects.Buttons;

import GameRun.Game;

public class StartButton extends Button {
    public StartButton(int x, int y, int width, int height, Game game) {
        super(x, y, width, height, "Start Game", "#00FF00", game);
    }

    @Override
    public void buttonAction() {
        System.out.println("Starting the game...");
        this.game.changeState("GameStarted.0");
    }
}
