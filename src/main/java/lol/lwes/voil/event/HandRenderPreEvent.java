package lol.lwes.voil.event;

import net.minecraft.client.render.VertexConsumerProvider;

public class HandRenderPreEvent extends HandRenderEvent {
    public HandRenderPreEvent(VertexConsumerProvider vertexconsumerprovider) {
        super(vertexconsumerprovider);
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    public void setCancelled() {
    }

    @Override
    public void setCancelled2(boolean flag) {
        this.setCancelled();
    }
}
