package lol.lwes.voil.hook;

import lol.lwes.voil.VoilClient;
import lol.lwes.voil.event.EventBus;
import lol.lwes.voil.event.TaskExecuteEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class GenericEventHook {
    public static void onEvent(CallbackInfo callbackinfo) {
        EventBus eventbus = VoilClient.eventBus;
        TaskExecuteEvent taskexecuteevent = TaskExecuteEvent.getInstance();
        if (((TaskExecuteEvent)eventbus.post(taskexecuteevent)).isCancelled()) {
            callbackinfo.cancel();
        }
    }
}
