package lol.lwes.voil.hook;

import lol.lwes.voil.duck.EntityHolderAccessor;
import lol.lwes.voil.module.ModuleManager;
import lol.lwes.voil.module.render.NameTags;
import lol.lwes.voil.util.target.TargetManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;

public abstract class EntityRendererHooks {
    public static void onUpdateRenderState(Entity entity, EntityRenderState entityrenderstate) {
        ((EntityHolderAccessor)entityrenderstate).tulip_setEntity(entity);
    }

    public static void renderLabelIfPresent() {
    }

    private static boolean shouldReplaceLabel(Entity entity) {
        return ModuleManager.INSTANCE.isEnabled(NameTags.class) && TargetManager.isTarget(entity, true);
    }
}
