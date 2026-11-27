package lol.lwes.voil.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lol.lwes.voil.hook.ItemHooks;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class ItemMixin {
    @ModifyExpressionValue(
        method = "raycast",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getRotationVector(FF)Lnet/minecraft/util/math/Vec3d;")
    )
    private static Vec3d silentRotationItemUse(Vec3d vec3d) {
        return ItemHooks.modifyRaycastRotation(vec3d);
    }
}
