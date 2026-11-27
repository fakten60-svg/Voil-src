package lol.lwes.voil.event;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class HeldItemRenderEvent extends Event {
    public AbstractClientPlayerEntity player;
    public float tickProgress;
    public float pitch;
    public Hand hand;
    public float swingProgress;
    public ItemStack stack;
    public float equipProgress;
    public MatrixStack matrices;
    public VertexConsumerProvider vertexConsumers;
    public int light;

    public HeldItemRenderEvent(
        AbstractClientPlayerEntity abstractclientplayerentity,
        float f,
        float f1,
        Hand hand,
        float f2,
        ItemStack itemstack,
        float f3,
        MatrixStack matrixstack,
        VertexConsumerProvider vertexconsumerprovider,
        int i
    ) {
        this.player = abstractclientplayerentity;
        this.tickProgress = f;
        this.pitch = f1;
        this.hand = hand;
        this.swingProgress = f2;
        this.stack = itemstack;
        this.equipProgress = f3;
        this.matrices = matrixstack;
        this.vertexConsumers = vertexconsumerprovider;
        this.light = i;
    }

    public void setEquipProgress(float f) {
        this.equipProgress = f;
    }
}
