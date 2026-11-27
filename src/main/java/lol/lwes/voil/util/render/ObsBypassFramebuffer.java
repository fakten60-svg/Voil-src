package lol.lwes.voil.util.render;

import lol.lwes.voil.util.render.shader.FramebufferBlitRenderer;

public class ObsBypassFramebuffer extends FramebufferBlitRenderer {
    public boolean useDefaultFramebuffer;

    @Override
    protected int getTextureId() {
        return this.useDefaultFramebuffer ? 0 : RenderManager.getCurrentFramebuffer();
    }
}
