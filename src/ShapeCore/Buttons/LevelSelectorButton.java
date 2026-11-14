package ShapeCore.Buttons;

import GameRun.Game;
import java.awt.Color;

public class LevelSelectorButton extends Button {
    public LevelSelectorButton(int x, int y, int width, int height, Game game) {
        super(x, y, width, height, "Select Level", new Color(70, 130, 180), game);
    }

    @Override
    public void buttonAction() {
        game.openLevelSelector();
    }
}
