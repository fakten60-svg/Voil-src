package lol.lwes.voil.event;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class Render2DEvent extends Event {
    public MatrixStack matrices;
    public DrawContext context;
    public int width;
    public int height;

    public Render2DEvent(DrawContext drawcontext, MatrixStack matrixstack, int i, int j) {
        this.matrices = new MatrixStack();
        this.context = drawcontext;
        this.width = i;
        this.height = j;
    }

    public MatrixStack getMatrices() {
        return this.matrices;
    }

    public int getHeight() {
        return this.height;
    }

    public DrawContext getContext() {
        return this.context;
    }

    public int getWidth() {
        return this.width;
    }
}
