package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.RaycastEvent;
import lol.lwes.voil.module.Module;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.render.NoRender;
import lol.lwes.voil.util.render.GuiScale;
import lol.lwes.voil.util.render.RenderManager;
import lol.lwes.voil.util.render.RenderSpace;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public abstract class GameRendererHooks {
    public static void onEndRender() {
        RenderManager.beginHud();
        RenderManager.flush();
        RenderManager.guiRendered = false;
        GuiScale.pushScale(GuiScale.getGuiScale());

        try {
            RenderManager.beginHud();

            for (Module module : ModuleManager.INSTANCE.getEnabledModules()) {
                VoilClient.getHudLayout().addModule(module);
            }

            RenderManager.endFrame(RenderSpace.HUD);
        } finally {
            GuiScale.popScale();
        }
    }

    public static void onUpdateCrosshairTargetPre(float f, CallbackInfo callbackinfo) {
        MinecraftClient minecraftclient = MinecraftClient.getInstance();
        Entity entity = minecraftclient.getCameraEntity();
        ClientPlayerEntity clientplayerentity = minecraftclient.player;
        if (entity != null && clientplayerentity != null && minecraftclient.world != null) {
            RaycastEvent raycastevent = RaycastEvent.create(entity);
            VoilClient.eventBus.postObject(raycastevent);
            if (raycastevent.hasFilter()) {
                double d0 = clientplayerentity.getEntityInteractionRange();
                Vec3d vec3d = entity.getCameraPosVec(f);
                Vec3d vec3d1 = entity.getRotationVec(f);
                Box box = entity.getBoundingBox().stretch(vec3d1.multiply(d0)).expand(1.0);
                EntityHitResult entityhitresult = ProjectileUtil.raycast(
                    entity, vec3d, vec3d.add(vec3d1.multiply(d0)), box, target -> EntityPredicates.CAN_HIT.test(target) && raycastevent.test(target), d0 * d0
                );
                if (entityhitresult != null) {
                    setCrosshairTarget(minecraftclient, entityhitresult);
                    callbackinfo.cancel();
                }
            }
        }
    }

    public static void onShowFloatingItem(CallbackInfo callbackinfo) {
        NoRender norender = (NoRender)ModuleManager.INSTANCE.getModule(NoRender.class);
        if (norender != null && norender.isEnabled() && norender.totemAnimation.isEnabled()) {
            callbackinfo.cancel();
        }
    }

    private static void setCrosshairTarget(MinecraftClient minecraftclient, HitResult hitresult) {
        minecraftclient.crosshairTarget = hitresult;
        if (hitresult instanceof EntityHitResult entityhitresult) {
            minecraftclient.targetedEntity = entityhitresult.getEntity();
        } else {
            minecraftclient.targetedEntity = null;
        }
    }
}
