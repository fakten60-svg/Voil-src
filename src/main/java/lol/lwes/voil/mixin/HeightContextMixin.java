package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.HeightContextHooks;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HeightContext.class)
public abstract class HeightContextMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;getMinimumY()I"))
    private int onMinY(ChunkGenerator chunkgenerator) {
        return HeightContextHooks.getMinimumY(chunkgenerator);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;getWorldHeight()I"))
    private int onHeight(ChunkGenerator chunkgenerator) {
        return HeightContextHooks.getWorldHeight(chunkgenerator);
    }
}
