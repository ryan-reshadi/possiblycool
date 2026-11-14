package ShapeCore.Buttons;

import GameRun.Game;
import GameRun.InputFrame;
import GameRun.LevelDefinition;
import java.awt.Color;

public class LevelSelectButton extends Button {
    private final String levelId;
    private final boolean available;

    public LevelSelectButton(int x, int y, int width, int height,
            LevelDefinition level, Game game) {
        super(x, y, width, height,
                level.isAvailable() ? level.getTitle() : level.getTitle() + " (Locked)",
                level.isAvailable() ? new Color(70, 130, 180) : new Color(90, 90, 95),
                game);
        this.levelId = level.getId();
        this.available = level.isAvailable();
    }

    @Override
    public void update(InputFrame input, int tickCount) {
        if (available) {
            super.update(input, tickCount);
        }
    }

    @Override
    public void buttonAction() {
        if (available) {
            game.chooseLevel(levelId);
        }
    }
}
