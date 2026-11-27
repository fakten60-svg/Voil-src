package lol.lwes.voil.gui.clickgui.page;

import lol.lwes.voil.gui.clickgui.ClickGuiPanel;
import lol.lwes.voil.gui.clickgui.ClickGuiScreen;
import net.minecraft.client.util.math.MatrixStack;

public class SocialPage extends ClickGuiPanel {
    private final ClickGuiScreen gui;

    public SocialPage(ClickGuiScreen clickguiscreen) {
        super(clickguiscreen);
        this.gui = clickguiscreen;
    }

    @Override
    public void render() {
    }

    @Override
    public void mouseDragged() {
    }

    @Override
    public void render2(MatrixStack matrixstack, int i, int j, float f) {
        this.render();
    }

    @Override
    public void mouseDragged2(double d0, double d1, int i, double d2, double d3) {
        this.mouseDragged();
    }
}
