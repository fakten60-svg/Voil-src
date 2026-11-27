package lol.lwes.voil.gui.clickgui;

import java.util.Objects;
import lol.lwes.voil.config.CloudConfig;

public record ConfigButton(ConfigAction action, CloudConfig config, float posX, float posY, float width, float height) {
    @Override
    public final boolean equals(Object object) {
        return this == object
            || object instanceof ConfigButton configbutton1
                && Float.compare(this.height, configbutton1.height) == 0
                && Float.compare(this.width, configbutton1.width) == 0
                && Float.compare(this.posY, configbutton1.posY) == 0
                && Float.compare(this.posX, configbutton1.posX) == 0
                && Objects.equals(this.config, configbutton1.config)
                && Objects.equals(this.action, configbutton1.action);
    }

    @Override
    public final String toString() {
        return "ib["
            + "ab="
            + this.action
            + ", ac="
            + this.config
            + ", ad="
            + this.posX
            + ", ae="
            + this.posY
            + ", af="
            + this.width
            + ", ag="
            + this.height
            + "]";
    }

    @Override
    public final int hashCode() {
        int i = 0 + Objects.hashCode(this.action);
        i = i * 31 + Objects.hashCode(this.config);
        i = i * 31 + Float.hashCode(this.posX);
        i = i * 31 + Float.hashCode(this.posY);
        i = i * 31 + Float.hashCode(this.width);
        return i * 31 + Float.hashCode(this.height);
    }

    public float getPosX() {
        return this.posX;
    }

    public float getPosY() {
        return this.posY;
    }

    public boolean contains(double d0, double d1) {
        return d0 >= this.posX && d0 <= this.posX + this.width && d1 >= this.posY && d1 <= this.posY + this.height;
    }

    public float getWidth() {
        return this.width;
    }

    public CloudConfig getConfig() {
        return this.config;
    }

    public float getHeight() {
        return this.height;
    }

    public ConfigAction getAction() {
        return this.action;
    }
}
