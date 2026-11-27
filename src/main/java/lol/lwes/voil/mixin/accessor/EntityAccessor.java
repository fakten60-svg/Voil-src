package lol.lwes.voil.mixin.accessor;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker("fall")
    void invokeFall(double d0, boolean flag, BlockState blockstate, BlockPos blockpos);
}
