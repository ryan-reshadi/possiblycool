package Objects.PlayerEquipment.Abilities;

import Levels.BaseLevel;
import Objects.Effects.TricksterArrowEffect;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Ability;

import java.awt.Color;

public class TrickstersArrowAbility extends Ability {

    private static final int COOLDOWN = 360; // 6 seconds

    public TrickstersArrowAbility() {
        super(COOLDOWN);
        this.iconColor = new Color(220, 190, 70);
    }

    @Override
    public void activate(Player player, BaseLevel level, int currentTick) {
        if (!isReady(currentTick)) return;
        lastUsedTick = currentTick;

        int px = player.getX() + player.getWidth() / 2;
        int py = player.getY() + player.getHeight() / 2;
        TricksterArrowEffect arrow = new TricksterArrowEffect(px, py, targetX, targetY, level.getEnemies(), player);
        level.queueAddVisualObject(arrow);
    }

    @Override
    public String getName() {
        return "Trickster's Arrow";
    }

    @Override
    public String getDescription() {
        return "Chain an arrow between up to five enemies, adding 10 damage per hit. Cooldown: 6s";
    }
}
