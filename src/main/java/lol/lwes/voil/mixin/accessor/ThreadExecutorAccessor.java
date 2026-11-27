package lol.lwes.voil.mixin.accessor;

import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ThreadExecutor.class)
public interface ThreadExecutorAccessor {
    @Invoker("runTasks")
    void invokeDaRunTasks();
}
