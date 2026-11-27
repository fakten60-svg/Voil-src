package dev.prestigeclient.renderlib.impl.render.events;

import dev.prestigeclient.renderlib.impl.api.RenderAPI;
import dev.prestigeclient.renderlib.impl.render.layer.type.BufferCollector;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface RenderEvents {
    Event<RenderEvents.RenderCallback> HUD_RENDER = makeSimpleEvent();
    Event<RenderEvents.RenderCallback> WORLD_RENDER = makeSimpleEvent();
    Event<RenderEvents.RenderCallback> RENDER_POST = makeSimpleEvent();
    Event<RenderEvents.RenderCallback> AFTER_WORLD = makeSimpleEvent();

    private static Event<RenderEvents.RenderCallback> makeSimpleEvent() {
        return EventFactory.createArrayBacked(RenderEvents.RenderCallback.class, callbacks -> (buffercollector, rendercontext) -> {
            for (RenderEvents.RenderCallback callback : callbacks) {
                callback.draw(buffercollector, rendercontext);
            }
        });
    }

    interface RenderCallback {
        void draw(BufferCollector buffercollector, RenderAPI.RenderContext rendercontext);
    }
}
