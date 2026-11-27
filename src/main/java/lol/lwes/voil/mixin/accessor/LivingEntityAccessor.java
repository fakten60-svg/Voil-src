package lol.lwes.voil.mixin.accessor;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("bodyYaw")
    void setBodyYaw(float f);

    @Accessor("jumpingCooldown")
    void setJumpingCooldown(int i);

    @Accessor("jumpingCooldown")
    int getJumpingCooldown();
}
