package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventBus;
import lol.lwes.voil.event.HandRenderPostEvent;
import lol.lwes.voil.event.HandRenderPreEvent;
import lol.lwes.voil.event.HeldItemRenderEvent;
import lol.lwes.voil.event.SwingProgressEvent;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.render.SwingAnimation;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class HeldItemRendererHooks {
    @Unique
    private static float equipProgress = 1.0F;
    @Unique
    private static float[] savedRotation = new float[4];
    @Unique
    private static boolean rotationOverrideActive = false;

    public static void restoreHandRotation(ClientPlayerEntity clientplayerentity) {
        clientplayerentity.lastRenderPitch = savedRotation[0];
        clientplayerentity.renderPitch = savedRotation[1];
        clientplayerentity.lastRenderYaw = savedRotation[2];
        clientplayerentity.renderYaw = savedRotation[3];
    }

    public static void onRenderItemTail() {
        EventBus eventbus = VoilClient.eventBus;
        HandRenderPreEvent handrenderpreevent = new HandRenderPreEvent(VoilClient.mc.getBufferBuilders().getEntityVertexConsumers());
        eventbus.post(handrenderpreevent);
    }

    public static void onRenderItemEnd(ItemStack itemstack, MatrixStack matrixstack) {
        if (itemstack.contains(DataComponentTypes.WEAPON)) {
            SwingAnimation swinganimation = (SwingAnimation)ModuleManager.INSTANCE.getModule(SwingAnimation.class);
            if (swinganimation != null && swinganimation.isEnabled()) {
                matrixstack.pop();
            }
        }
    }

    public static float modifySwingProgress(float f) {
        Object object = null;
        SwingProgressEvent swingprogressevent = new SwingProgressEvent(f);
        VoilClient.eventBus.post(swingprogressevent);
        return swingprogressevent.isCancelled() ? 0.0F : swingprogressevent.getProgress();
    }

    public static void saveHandRotation(float f, ClientPlayerEntity clientplayerentity) {
        VoilClient voilclient = VoilClient.getInstance();
        boolean flag = voilclient.rotationManager.isActive();
        savedRotation[0] = clientplayerentity.lastRenderPitch;
        savedRotation[1] = clientplayerentity.renderPitch;
        savedRotation[2] = clientplayerentity.lastRenderYaw;
        savedRotation[3] = clientplayerentity.renderYaw;
        if (flag) {
            clientplayerentity.lastRenderPitch = clientplayerentity.getPitch();
            clientplayerentity.renderPitch = clientplayerentity.getPitch();
            clientplayerentity.lastRenderYaw = clientplayerentity.getYaw();
            clientplayerentity.renderYaw = clientplayerentity.getYaw();
        } else if (rotationOverrideActive) {
            savedRotation[1] = lerp(f, savedRotation[1], clientplayerentity.getPitch());
            savedRotation[3] = lerp(f, savedRotation[3], clientplayerentity.getYaw());
        }

        rotationOverrideActive = flag;
    }

    @Unique
    private static float lerp(float f, float f1, float f2) {
        return f1 + f * (f2 - f1);
    }

    public static void onRenderFirstPersonItem() {
        EventBus eventbus = VoilClient.eventBus;
        HandRenderPostEvent handrenderpostevent = new HandRenderPostEvent(VoilClient.mc.getBufferBuilders().getEntityVertexConsumers());
        eventbus.post(handrenderpostevent);
    }

    public static float modifyEquipProgress() {
        return equipProgress;
    }

    public static void onRenderItemStart(
        AbstractClientPlayerEntity abstractclientplayerentity,
        float f,
        float f1,
        Hand hand,
        float f2,
        ItemStack itemstack,
        float f3,
        MatrixStack matrixstack,
        int i,
        CallbackInfo callbackinfo
    ) {
        Object object = null;
        HeldItemRenderEvent helditemrenderevent = new HeldItemRenderEvent(
            abstractclientplayerentity, f, f1, hand, f2, itemstack, f3, matrixstack, VoilClient.mc.getBufferBuilders().getEntityVertexConsumers(), i
        );
        VoilClient.eventBus.post(helditemrenderevent);
        if (helditemrenderevent.isCancelled()) {
            callbackinfo.cancel();
        }

        equipProgress = helditemrenderevent.equipProgress;
    }
}
