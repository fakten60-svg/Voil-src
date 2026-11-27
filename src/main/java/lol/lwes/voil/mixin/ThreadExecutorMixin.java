package lol.lwes.voil.mixin;

import lol.lwes.voil.hook.GenericEventHook;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThreadExecutor.class)
public abstract class ThreadExecutorMixin {
    @Inject(method = "runTasks()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/ThreadExecutor;runTask()Z"), cancellable = true)
    private void onFrameTasks(CallbackInfo callbackinfo) {
        GenericEventHook.onEvent(callbackinfo);
    }
}
