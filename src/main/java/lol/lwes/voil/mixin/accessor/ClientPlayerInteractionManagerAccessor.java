package lol.lwes.voil.mixin.accessor;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerInteractionManager.class)
public interface ClientPlayerInteractionManagerAccessor {
    @Invoker("syncSelectedSlot")
    void syncSlot();

    @Accessor("currentBreakingProgress")
    float getCurBlockDamageMP();

    @Accessor("currentBreakingProgress")
    void setCurBlockDamageMP(float f);

    @Accessor("currentBreakingPos")
    BlockPos getCurrentBreakingPos();
}
