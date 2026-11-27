package lol.lwes.voil.util.render;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.mgr.RenderPass;
import dev.prestigeclient.renderlib.impl.render.state.StateApplier;
import lol.lwes.voil.VoilClient;
import lol.lwes.voil.util.render.shader.FramebufferBlitRenderer;
import org.lwjgl.opengl.GL33;

public class FramebufferStateApplier implements StateApplier {
    final FramebufferBlitRenderer framebuffer;

    public FramebufferStateApplier(FramebufferBlitRenderer framebufferblitrenderer) {
        this.framebuffer = framebufferblitrenderer;
    }

    @Override
    public void apply(RenderPass renderpass) {
        GL33.glViewport(0, 0, VoilClient.mc.getWindow().getFramebufferWidth(), VoilClient.mc.getWindow().getFramebufferHeight());
    }

    @Override
    public void revert() {
        if (FramebufferBlitRenderer.getFramebuffer(this.framebuffer) != null) {
            int i = RenderAPI.getMinecraftFramebuffer();
            GL33.glBindFramebuffer(36160, this.framebuffer.getHeight());
            GL33.glViewport(0, 0, VoilClient.mc.getWindow().getFramebufferWidth(), VoilClient.mc.getWindow().getFramebufferHeight());
            GL33.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            GL33.glDisable(3089);
            GL33.glClear(16384);
            GL33.glBindFramebuffer(36160, i);
        }
    }
}
