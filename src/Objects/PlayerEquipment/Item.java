package Objects.PlayerEquipment;

import Objects.PlayerClasses.Player;
import Objects.VisualObject;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public abstract class Item {

    protected String name = "Item";
    protected String description = "";
    protected Color iconColor = Color.GRAY;
    protected String spritePath = null;

    public Item(boolean visible, String imgpath) {
        if (!visible) {
            throw new IllegalArgumentException("Bad Input; This constructor is ONLY for visible items, first argument should be true");
        }
        this.spritePath = imgpath;
    }

    public Item() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Color getIconColor() {
        return iconColor;
    }

    public void setIconColor(Color iconColor) {
        this.iconColor = iconColor;
    }

    public String getSpritePath() {
        return spritePath;
    }

    public void setSpritePath(String spritePath) {
        this.spritePath = spritePath;
    }

    public void tick(int currentTick, int clickXDown, int clickYDown, int clickXUp, int clickYUp, ArrayList<VisualObject> targets, Player owner) {
        if (clickXDown > -1 || clickYDown > -1) {
            this.leftClickAct(clickXDown, clickYDown, clickXUp, clickYUp, currentTick, owner.getX() + owner.getWidth() / 2, owner.getY() + owner.getHeight() / 2);
        }
    }

    public void leftClickAct(int clickXDown, int clickYDown, int clickXUp, int clickYUp, int currentTick, int processedX, int processedY) {
    }

    public void draw(Graphics g) {
    }
}
