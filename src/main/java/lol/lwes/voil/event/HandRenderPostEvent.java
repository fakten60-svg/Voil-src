package lol.lwes.voil.event;

import net.minecraft.client.render.VertexConsumerProvider;

public class HandRenderPostEvent extends HandRenderEvent {
    public HandRenderPostEvent(VertexConsumerProvider vertexconsumerprovider) {
        super(vertexconsumerprovider);
    }
}
