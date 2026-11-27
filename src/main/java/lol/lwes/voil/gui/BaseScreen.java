package lol.lwes.voil.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class BaseScreen extends Screen {
    public BaseScreen(Text text) {
        super(text);
    }

    @Override
    protected void renderDarkening(DrawContext drawcontext, int i, int j, int k, int l) {
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderInGameBackground(DrawContext drawcontext) {
    }

    @Override
    protected void applyBlur(DrawContext drawcontext) {
    }
}
