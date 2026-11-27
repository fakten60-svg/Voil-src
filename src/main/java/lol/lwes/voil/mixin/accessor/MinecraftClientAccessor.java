package lol.lwes.voil.mixin.accessor;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("itemUseCooldown")
    int getItemUseCooldown();

    @Invoker("doAttack")
    boolean invokeDoAttack();

    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int i);

    @Invoker("doItemUse")
    void invokeDoItemUse();
}
