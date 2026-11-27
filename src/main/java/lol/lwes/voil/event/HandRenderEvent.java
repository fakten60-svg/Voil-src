package lol.lwes.voil.event;

import net.minecraft.client.render.VertexConsumerProvider;

public class HandRenderEvent extends Event {
    private VertexConsumerProvider vertexConsumers;

    public HandRenderEvent(VertexConsumerProvider vertexconsumerprovider) {
        this.vertexConsumers = vertexconsumerprovider;
    }

    public VertexConsumerProvider getVertexConsumers() {
        return this.vertexConsumers;
    }

    public void setVertexConsumers(VertexConsumerProvider vertexconsumerprovider) {
        this.vertexConsumers = vertexconsumerprovider;
    }
}
