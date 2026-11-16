package Objects.PlayerEquipment.Abilities;

import Levels.BaseLevel;
import Objects.Effects.RallyBarricade;
import Objects.PlayerClasses.Player;
import Objects.PlayerEquipment.Ability;

import java.awt.Color;

public class RallyBarricadeAbility extends Ability {

    private static final int COOLDOWN = 480; // 8 seconds

    public RallyBarricadeAbility() {
        super(COOLDOWN);
        this.iconColor = new Color(190, 130, 70);
    }

    @Override
    public void activate(Player player, BaseLevel level, int currentTick) {
        if (!isReady(currentTick)) return;
        lastUsedTick = currentTick;
        level.queueAddVisualObject(new RallyBarricade(targetX, targetY, player, level.getEnemies(), currentTick));
    }

    @Override
    public String getName() {
        return "Rally Barricade";
    }

    @Override
    public String getDescription() {
        return "Create an 8-second projectile-blocking barricade. It heals you and damages enemies crossing it. Cooldown: 8s";
    }
}
